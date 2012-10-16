package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.BillDeposit;
import models.db.LgDeposit;
import play.mvc.Controller;

public class ReportController extends Controller {

    public static void index() {
        mainMenu();
    }

    public static void mainMenu() {
        render();
    }

    static public class DepositData {

        final public Integer id;
        final public String type;
        final public Long total;
        final public Date startDate;
        final public Date finishDate;

        public DepositData(LgDeposit d) {
            if (d instanceof BillDeposit) {
                this.total = ((BillDeposit) d).getTotal();
                this.type = "Bill";
            } else {
                this.total = new Long(0);
                this.type = "Envelope";
            }
            this.id = d.depositId;
            this.startDate = d.startDate;
            this.finishDate = d.finishDate;
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
