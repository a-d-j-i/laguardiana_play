package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.Bill;
import models.BillDeposit;
import models.Configuration;
import models.EnvelopeDeposit;
import models.db.LgBillType;
import models.db.LgDeposit;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.lov.Currency;
import play.Logger;
import play.data.binding.As;
import play.mvc.Before;
import play.mvc.Controller;

public class ReportDepositController extends Controller {

    @Before
    public static void setFormat(String format) {
        if (format != null) {
            request.format = format;
        }
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

    public static void unprocessed(Integer page) {
        if (request.format.equalsIgnoreCase("html")) {
            if (page == null || page < 1) {
                page = 1;
            }
            int length = 4;
            List<LgDeposit> depositList = LgDeposit.findUnprocessed(Configuration.EXTERNAL_APP_ID).fetch(page, length);
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

    public static void process(Integer depositId) {
        if (depositId == null) {
            unprocessed(1);
        }
        boolean stat = LgDeposit.process(Configuration.EXTERNAL_APP_ID, depositId, "DONE");
        if (request.format.equalsIgnoreCase("html")) {
            unprocessed(1);
        } else {
            renderHtml(stat ? "DONE" : "ERROR");
        }
    }

    public static void list(Integer page, @As("dd/MM/yyyy") Date startDate, @As("dd/MM/yyyy") Date endDate) {
        if (endDate == null) {
            endDate = new Date();
        }
        if (page == null || page < 1) {
            page = 1;
        }
        int length = 3;
        Integer totalPage = (int) (LgDeposit.count(startDate, endDate) / length) + 1;
        if (page > totalPage) {
            page = totalPage;
        }
        renderArgs.put("prevPage", page - 1);
        renderArgs.put("nextPage", page + 1);

        List<LgDeposit> dList = LgDeposit.find(startDate, endDate).fetch(page, length);
        renderArgs.put("startDate", startDate);
        renderArgs.put("endDate", endDate);
        renderArgs.put("page", page);
        renderArgs.put("totalPage", totalPage);
        renderArgs.put("data", dList);
        flash.put("backUrl", request.url);
        render();
    }

    public static void detail(Integer depositId) {
        LgDeposit d = LgDeposit.findById(depositId);
        d.setRenderArgs(renderArgs.data);
        Logger.debug("-------- BACK URL -> %s", flash.get("backUrl"));
        renderArgs.put("backUrl", flash.get("backUrl"));
        render(d.getDetailView());
    }

    public static void reprint(Integer depositId) {
        LgDeposit d = LgDeposit.findById(depositId);
        d.print(true);
        list(depositId, null, null);
    }
}
