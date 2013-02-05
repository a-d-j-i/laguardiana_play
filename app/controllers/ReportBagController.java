package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.Configuration;
import models.db.LgBag;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.Router;

public class ReportBagController extends Controller {

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

    public static void unprocessed(Integer page) {
        if (request.format.equalsIgnoreCase("html")) {
            if (page == null || page < 1) {
                page = 1;
            }
            int length = 4;
            List<LgBag> bagList = LgBag.findUnprocessed(Configuration.EXTERNAL_APP_ID).fetch(page, length);
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
        List<LgBag> bagList = LgBag.findUnprocessed(Configuration.EXTERNAL_APP_ID).fetch();
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

    public static void process(Integer bagId) {
        if (bagId == null) {
            unprocessed(1);
        }
        boolean stat = LgBag.process(Configuration.EXTERNAL_APP_ID, bagId, "DONE");
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
        Integer totalPage = (int) (LgBag.count(startDate, endDate) / length) + 1;
        if (page > totalPage) {
            page = totalPage;
        }
        renderArgs.put("prevPage", page - 1);
        renderArgs.put("nextPage", page + 1);

        List<LgBag> bList = LgBag.find(startDate, endDate).fetch(page, length);
        renderArgs.put("startDate", startDate);
        renderArgs.put("endDate", endDate);
        renderArgs.put("page", page);
        renderArgs.put("totalPage", totalPage);
        renderArgs.put("data", bList);
        flash.put("backUrl", request.url);
        render();
    }

    public static void detail(Integer bId) {
        LgBag b;
        if (bId == null) {
            b = LgBag.getCurrentBag();
        } else {
            b = LgBag.findById(bId);
        }
        renderArgs.put("backUrl", flash.get("backUrl"));
        b.setRenderArgs(renderArgs.data);
        render();
    }

    public static void reprint(Integer bId) {
        LgBag b;
        if (bId == null) {
            b = LgBag.getCurrentBag();
        } else {
            b = LgBag.findById(bId);
        }
        b.print(true);
        list(bId, null, null);
    }

    public static void rotateBag() {
        LgBag.rotateBag(false);
        MenuController.accountingMenu(null);
    }

    public static void print() {
        LgBag currentBag = LgBag.getCurrentBag();
        currentBag.print(false);
        currentBag.setRenderArgs(renderArgs.data);
        flash.put("backUrl", Router.reverse("MenuController.AccountingMenu"));
        detail(currentBag.bagId);
    }
}
