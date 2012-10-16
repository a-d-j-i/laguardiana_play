package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import models.BillDeposit;
import models.EnvelopeDeposit;
import models.db.LgBill;
import models.db.LgDeposit;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
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

        final public Integer type;
        final public Integer amount;
        final public Integer currency;

        private DepositDataContents(LgEnvelopeContent c) {
            this.type = c.contentTypeLov;
            this.amount = c.amount;
            this.currency = c.unitLov;
        }
    }

    static public class DepositData {

        final public Integer id;
        final public String type;
        final public Long total;
        final public Integer currency;
        final public Date startDate;
        final public Date finishDate;
        final List<DepositDataContents> contents;

        public DepositData(LgDeposit d) {
            this.id = d.depositId;
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
                    this.currency = lb.billType.currency;
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
        LgDeposit.process(2, depositId, "DONE");
        if (request.format.equalsIgnoreCase("html")) {
            unprocessedDeposits(1);
        } else {
            renderHtml("DONE");
        }
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
        List<DepositData> ret = new ArrayList<DepositData>(depositList.size());
        for (LgDeposit d : depositList) {
            ret.add(new DepositData(d));
        }
        if (request.format.equalsIgnoreCase("xml")) {
            renderXml(ret);
        } else {
            renderJSON(ret);
        }
    }
}
