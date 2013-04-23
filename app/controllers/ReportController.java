package controllers;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.Bill;
import models.BillDeposit;
import models.Configuration;
import models.EnvelopeDeposit;
import models.db.LgBag;
import models.db.LgBillType;
import models.db.LgDeposit;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.db.LgEvent;
import models.db.LgZ;
import models.lov.Currency;
import play.Logger;
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
            if (c.amount != null) {
                this.amount = new Double(c.amount);
            } else {
                this.amount = new Double(0);
            }
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
        final public String gecos;
        final public Date startDate;
        final public Date finishDate;

        public DepositData(LgDeposit d, Integer type) {
            this.type = type;
            this.id = d.depositId;
            this.bag_id = d.bag.bagId;
            this.user_id = d.user.externalId;
            this.gecos = d.user.gecos;
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
        } else {
            List<LgDeposit> depositList = LgDeposit.findUnprocessed(2).fetch(50);
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
    }

    static public class BagData {

        public Integer bagId;
        public Date creationDate;
        public Date withdrawDate;
        public final String userId;

        private BagData(LgBag b) {
            this.bagId = b.bagId;
            this.creationDate = b.creationDate;
            this.withdrawDate = b.withdrawDate;
            this.userId = b.withdrawUser;
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
        } else {
            List<LgBag> bagList = LgBag.findUnprocessed(EXTERNAL_APP_ID).fetch(50);
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
        } else {
            List<LgZ> zList = LgZ.findUnprocessed(EXTERNAL_APP_ID).fetch(50);
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
    }

    static public class EventData {

        final public Integer eventId;
        final public Date creationDate;
        final public String user_id;
        final public String gecos;
        final public Integer eventSourceId;
        final public String message;

        private EventData(LgEvent e) {
            this.eventId = e.eventId;
            this.creationDate = e.creationDate;
            if (e.user != null) {
                this.user_id = e.user.username;
                this.gecos = e.user.gecos;
            } else {
                this.user_id = null;
                this.gecos = null;
            }
            this.eventSourceId = e.eventSourceId;
            this.message = e.message;
        }
    }

    static public class EventList {

        final public String clientCode = Configuration.getClientCode();
        final public String branchCode = Configuration.getBranchCode();
        final public String machineCode = Configuration.getMachineCode();
        final public String machineDescription = Configuration.getMachineDescription();
        public List<EventData> eventData;
    }

    public static void eventList(Integer page) {
        unprocessedEvents(page);
    }

    public static void unprocessedEvents(Integer page) {
        if (request.format.equalsIgnoreCase("html")) {
            if (page == null || page < 1) {
                page = 1;
            }
            int length = 4;
            List<LgEvent> eventList = LgEvent.findUnprocessed(EXTERNAL_APP_ID).fetch(page, length);
            if (page > 1) {
                renderArgs.put("prevPage", page - 1);
            } else {
                renderArgs.put("prevPage", 1);
            }
            if (eventList.size() == length) {
                renderArgs.put("nextPage", page + 1);
            } else {
                renderArgs.put("nextPage", page);
            }
            renderArgs.put("data", eventList);
            render();
            return;
        } else {
            List<LgEvent> eventList = LgEvent.findUnprocessed(EXTERNAL_APP_ID).fetch(50);
            EventList ret = new EventList();
            ret.eventData = new ArrayList<EventData>(eventList.size());
            for (LgEvent e : eventList) {
                ret.eventData.add(new EventData(e));
            }
            if (request.format.equalsIgnoreCase("xml")) {
                renderXml(ret);
            } else {
                renderJSON(ret);
            }
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

    public static void processEvent(Integer eventId) {
        if (eventId == null) {
            unprocessedEvents(1);
        }
        boolean stat = LgEvent.process(EXTERNAL_APP_ID, eventId, "DONE");
        if (request.format.equalsIgnoreCase("html")) {
            unprocessedEvents(1);
        } else {
            renderHtml(stat ? "DONE" : "ERROR");
        }
    }

    protected static void renderXml(Object o) {
        XStream xStream = new XStream();
        xStream.registerConverter(new Converter() {
            public boolean canConvert(Class clazz) {
                return clazz.equals(Double.class);
            }

            public void marshal(Object value, HierarchicalStreamWriter writer,
                    MarshallingContext context) {
                DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
                df.setMaximumFractionDigits(1000);
                df.setMaximumIntegerDigits(1000);
                df.setGroupingUsed(false);
                writer.setValue(df.format((Double) value));
            }

            public Object unmarshal(HierarchicalStreamReader reader,
                    UnmarshallingContext context) {
                return null;
            }
        });
        renderXml(o, xStream);
    }
}
