package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import models.Bill;
import models.BillDeposit;
import models.Configuration;
import models.EnvelopeDeposit;
import models.db.LgBag;
import models.db.LgBill;
import models.db.LgBillType;
import models.db.LgDeposit;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.db.LgZ;
import models.lov.Currency;
import play.Logger;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;

public class ReportController extends Controller {

    final static int EXTERNAL_APP_ID = 2;

    @Before
    public static void setFormat(String format) {
        if (format != null) {
            request.format = format;
        }
    }

    public static F.T3<Long, Long, Map<Currency, Map<LgBillType, Bill>>> getTotals(Set<LgDeposit> deps) {
        long envelopes = 0;
        long deposits = 0;
        Map<Currency, Map<LgBillType, Bill>> totals = new HashMap();
        for (LgDeposit d : deps) {
            deposits++;
            if (d instanceof BillDeposit) {
                BillDeposit bd = (BillDeposit) d;
                for (LgBill b : bd.bills) {
                    Currency c = b.billType.getCurrency();
                    Map<LgBillType, Bill> ct = totals.get(c);
                    if (ct == null) {
                        ct = new HashMap();
                    }
                    Bill bill = ct.get(b.billType);
                    if (bill == null) {
                        bill = new Bill(b.billType);
                    }
                    bill.q += b.quantity;
                    ct.put(b.billType, bill);
                    totals.put(c, ct);
                }
            } else if (d instanceof EnvelopeDeposit) {
                envelopes++;
            } else {
                Logger.error("Invalid deposit type");
            }
        }
        return new F.T3<Long, Long, Map<Currency, Map<LgBillType, Bill>>>(envelopes, deposits, totals);
    }

    static public class ContentData {
    }

    static public class BillContentData extends ContentData {

        final public String billType;
        final public Integer denomination;
        final public Integer quantity;

        public BillContentData(String billType, Integer denomination, Integer quantity) {
            this.billType = billType;
            this.denomination = denomination;
            this.quantity = quantity;
        }
    }

    static public class EnvelopeContentData extends ContentData {

        final public String type;
        final public Double amount;
        final public String currency;

        private EnvelopeContentData(LgEnvelopeContent c) {
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
        final public Integer type;
        final public Integer bag_id;
        final public String user_id;
        final public Date startDate;
        final public Date finishDate;

        public DepositData(LgDeposit d, Integer type) {
            this.type = type;
            this.id = d.depositId;
            this.bag_id = d.bag.bagId;
            this.user_id = d.user.externalId;
            this.startDate = d.startDate;
            this.finishDate = d.finishDate;
        }
    }

    static public class BillDepositData extends DepositData {

        public List<BillContentData> contents = new ArrayList<BillContentData>();

        public BillDepositData(BillDeposit d) {
            super(d, 0);
            List qret = Bill.getDepositContent(d);

            for (Object b : qret) {
                Object[] a = (Object[]) b;
                Long quantity = (Long) a[ 1];

                LgBillType bt = (LgBillType) a[0];
                BillContentData bdc = new BillContentData(Currency.findByNumericId(bt.unitLov).textId,
                        bt.denomination.intValue(), quantity.intValue());
                contents.add(bdc);
            }
        }
    }

    static public class EnvelopeDepositData extends DepositData {

        public String envelopeCode;
        public List<EnvelopeContentData> contents = new ArrayList<EnvelopeContentData>();

        public EnvelopeDepositData(EnvelopeDeposit d) {
            super(d, 1);
            for (LgEnvelope le : d.envelopes) {
                this.envelopeCode = le.envelopeNumber;
                for (LgEnvelopeContent c : le.envelopeContents) {
                    EnvelopeContentData cc = new EnvelopeContentData(c);
                    contents.add(cc);
                }
            }
        }
    }

    static public class UnprocessedDeposits {

        final public String clientCode = Configuration.getClientCode();
        final public String branchCode = Configuration.getBranchCode();
        final public String machineCode = Configuration.getMachineCode();
        final public String machineDescription = Configuration.getMachineDescription();
        public List<DepositData> depositData;
    }

    public static void unprocessedDeposits(Integer page) {
        if (request.format.equalsIgnoreCase("html")) {
            if (page == null || page < 1) {
                page = 1;
            }
            int length = 4;
            List<LgDeposit> depositList = LgDeposit.findUnprocessed(EXTERNAL_APP_ID).fetch(page, length);
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
            if (d instanceof BillDeposit) {
                ret.depositData.add(new BillDepositData((BillDeposit) d));
            } else if (d instanceof EnvelopeDeposit) {
                ret.depositData.add(new EnvelopeDepositData((EnvelopeDeposit) d));
            } else {
                Logger.error("Invalid deposit type");
            }
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

        final public String clientCode = Configuration.getClientCode();
        final public String branchCode = Configuration.getBranchCode();
        final public String machineCode = Configuration.getMachineCode();
        final public String machineDescription = Configuration.getMachineDescription();
        public List<BagData> bagData;
    }

    public static void bagList(Integer page) {
        unprocessedBags(page);
    }

    public static void unprocessedBags(Integer page) {
        if (request.format.equalsIgnoreCase("html")) {
            if (page == null || page < 1) {
                page = 1;
            }
            int length = 4;
            List<LgBag> bagList = LgBag.findUnprocessed(EXTERNAL_APP_ID).fetch(page, length);
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
        List<LgBag> bagList = LgBag.findUnprocessed(EXTERNAL_APP_ID).fetch();
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

    static public class ZData {

        public Integer ZId;
        public Date creationDate;
        public Date closeDate;

        private ZData(LgZ z) {
            this.ZId = z.zId;
            this.creationDate = z.creationDate;
            this.closeDate = z.closeDate;
        }
    }

    static public class ZList {

        final public String clientCode = Configuration.getClientCode();
        final public String branchCode = Configuration.getBranchCode();
        final public String machineCode = Configuration.getMachineCode();
        final public String machineDescription = Configuration.getMachineDescription();
        public List<ZData> zData;
    }

    public static void zList(Integer page) {
        unprocessedZs(page);
    }

    public static void unprocessedZs(Integer page) {
        if (request.format.equalsIgnoreCase("html")) {
            if (page == null || page < 1) {
                page = 1;
            }
            int length = 4;
            List<LgZ> zList = LgZ.findUnprocessed(EXTERNAL_APP_ID).fetch(page, length);
            if (page > 1) {
                renderArgs.put("prevPage", page - 1);
            } else {
                renderArgs.put("prevPage", 1);
            }
            if (zList.size() == length) {
                renderArgs.put("nextPage", page + 1);
            } else {
                renderArgs.put("nextPage", page);
            }
            renderArgs.put("data", zList);
            render();
            return;
        }
        List<LgZ> zList = LgZ.findUnprocessed(EXTERNAL_APP_ID).fetch();
        ZList ret = new ZList();
        ret.zData = new ArrayList<ZData>(zList.size());
        for (LgZ z : zList) {
            ret.zData.add(new ZData(z));
        }
        if (request.format.equalsIgnoreCase("xml")) {
            renderXml(ret);
        } else {
            renderJSON(ret);
        }
    }

    public static void processDeposit(Integer depositId) {
        if (depositId == null) {
            unprocessedDeposits(1);
        }
        boolean stat = LgDeposit.process(EXTERNAL_APP_ID, depositId, "DONE");
        if (request.format.equalsIgnoreCase("html")) {
            unprocessedDeposits(1);
        } else {
            renderHtml(stat ? "DONE" : "ERROR");
        }
    }

    public static void processBag(Integer bagId) {
        if (bagId == null) {
            unprocessedBags(1);
        }
        boolean stat = LgBag.process(EXTERNAL_APP_ID, bagId, "DONE");
        if (request.format.equalsIgnoreCase("html")) {
            unprocessedBags(1);
        } else {
            renderHtml(stat ? "DONE" : "ERROR");
        }
    }

    public static void processZ(Integer zId) {
        if (zId == null) {
            unprocessedZs(1);
        }
        boolean stat = LgZ.process(EXTERNAL_APP_ID, zId, "DONE");
        if (request.format.equalsIgnoreCase("html")) {
            unprocessedZs(1);
        } else {
            renderHtml(stat ? "DONE" : "ERROR");
        }
    }
}
