package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import models.BillDeposit;
import models.EnvelopeDeposit;
import models.db.LgBag;
import models.db.LgBill;
import models.db.LgDeposit;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.db.LgSystemProperty;
import models.lov.Currency;
import play.Logger;
import play.mvc.Controller;

public class ReportController extends Controller {

    public static void index() {
        mainMenu();
    }

    public static void mainMenu() {
        render();
    }

    static public class DepositDataContents {

        final public String type;
        final public Integer amount;
        final public String currency;

        private DepositDataContents(LgEnvelopeContent c) {
            this.type = LgEnvelopeContent.EnvelopeContentType.find(c.contentTypeLov).name();
            this.amount = c.amount;
            if (c.unitLov == null) {
                this.currency = "-";
            } else {
                this.currency = Currency.findByNumericId(c.unitLov).textId;
            }
        }
    }

    static public class DepositData {

        final public Integer id;
        final public String type;
        final public Long total;
        final public String currency;
        final public Integer bag_id;
        final public String user_id;
        final public Date startDate;
        final public Date finishDate;
        final List<DepositDataContents> contents;

        public DepositData(LgDeposit d) {
            this.id = d.depositId;
            this.bag_id = d.bag.bagId;
            this.user_id = d.user.externalId;
            this.startDate = d.startDate;
            this.finishDate = d.finishDate;
            if (d instanceof BillDeposit) {
                this.type = "Bill";
                this.contents = null;
                BillDeposit b = (BillDeposit) d;
                Iterator<LgBill> i = b.bills.iterator();
                if (i.hasNext()) {
                    LgBill lb = i.next();
                    this.total = b.getTotal();
                    if (lb.billType.currency == null) {
                        this.currency = "-";
                    } else {
                        this.currency = Currency.findByNumericId(lb.billType.currency).textId;
                    }
                } else {
                    this.total = null;
                    this.currency = null;
                }
            } else if (d instanceof EnvelopeDeposit) {
                this.type = "Envelope";
                EnvelopeDeposit e = (EnvelopeDeposit) d;
                this.total = null;
                this.currency = null;
                this.contents = new ArrayList<DepositDataContents>();
                Iterator<LgEnvelope> i = e.envelopes.iterator();
                while (i.hasNext()) {
                    LgEnvelope le = i.next();
                    for (LgEnvelopeContent c : le.envelopeContents) {
                        DepositDataContents cc = new DepositDataContents(c);
                        contents.add(cc);
                    }
                }
            } else {
                Logger.error("DepositData Invalid deposit type");
                this.type = "unknow";
                this.total = null;
                this.currency = null;
                this.contents = null;
            }
        }
    }

    public static void processDeposit(Integer depositId) {
        boolean stat = LgDeposit.process(2, depositId, "DONE");
        if (request.format.equalsIgnoreCase("html")) {
            unprocessedDeposits(1);
        } else {
            renderHtml(stat ? "DONE" : "ERROR");
        }
    }

    static public class UnprocessedDeposits {

        final public String clientCode = LgSystemProperty.getProperty(LgSystemProperty.Types.CLIENT_CODE);
        final public String machineCode = LgSystemProperty.getProperty(LgSystemProperty.Types.MACHINE_CODE);
        final public String machineDescription = LgSystemProperty.getProperty(LgSystemProperty.Types.MACHINE_DESCRIPTION);
        public List<DepositData> depositData;
    }

    public static void unprocessedDeposits(Integer page) {
        if (request.format.equalsIgnoreCase("html")) {
            if (page == null || page < 1) {
                page = 1;
            }
            int length = 4;
            List<LgDeposit> depositList = LgDeposit.findUnprocessed(2).fetch(page, length);
            if (page > 1) {
                renderArgs.put("prevPage", page - 1);
            } else {
                renderArgs.put("prevPage", 1);
            }
            if (depositList.size() == length) {
                renderArgs.put("nextPage", page + 1);
            } else {
                renderArgs.put("nextPage", page);
            }
            renderArgs.put("data", depositList);
            render();
            return;
        }
        List<LgDeposit> depositList = LgDeposit.findUnprocessed(2).fetch();
        UnprocessedDeposits ret = new UnprocessedDeposits();
        ret.depositData = new ArrayList<DepositData>(depositList.size());
        for (LgDeposit d : depositList) {
            ret.depositData.add(new DepositData(d));
        }
        if (request.format.equalsIgnoreCase("xml")) {
            renderXml(ret);
        } else {
            renderJSON(ret);
        }
    }

    static public class BagData {

        public Integer bagId;
        public Date creationDate;
        public Date withdrawDate;

        private BagData(LgBag b) {
            this.bagId = b.bagId;
            this.creationDate = b.creationDate;
            this.withdrawDate = b.withdrawDate;
        }
    }

    static public class BagList {

        final public String clientCode = LgSystemProperty.getProperty(LgSystemProperty.Types.CLIENT_CODE);
        final public String machineCode = LgSystemProperty.getProperty(LgSystemProperty.Types.MACHINE_CODE);
        final public String machineDescription = LgSystemProperty.getProperty(LgSystemProperty.Types.MACHINE_DESCRIPTION);
        public List<BagData> bagData;
    }

    public static void bagList(Integer page) {
        if (request.format.equalsIgnoreCase("html")) {
            if (page == null || page < 1) {
                page = 1;
            }
            int length = 4;
            List<LgBag> bagList = LgBag.findUnprocessed().fetch(page, length);
            if (page > 1) {
                renderArgs.put("prevPage", page - 1);
            } else {
                renderArgs.put("prevPage", 1);
            }
            if (bagList.size() == length) {
                renderArgs.put("nextPage", page + 1);
            } else {
                renderArgs.put("nextPage", page);
            }
            renderArgs.put("data", bagList);
            render();
            return;
        }
        List<LgBag> bagList = LgBag.findUnprocessed().fetch();
        BagList ret = new BagList();
        ret.bagData = new ArrayList<BagData>(bagList.size());
        for (LgBag b : bagList) {
            ret.bagData.add(new BagData(b));
        }
        if (request.format.equalsIgnoreCase("xml")) {
            renderXml(ret);
        } else {
            renderJSON(ret);
        }
    }
}
