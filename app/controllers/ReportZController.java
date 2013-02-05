package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.Configuration;
import models.db.LgZ;
import play.data.binding.As;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;

public class ReportZController extends Controller {

    @Before
    public static void setFormat(String format) {
        if (format != null) {
            request.format = format;
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

    public static void unprocessed(Integer page) {
        if (request.format.equalsIgnoreCase("html")) {
            if (page == null || page < 1) {
                page = 1;
            }
            int length = 4;
            List<LgZ> zList = LgZ.findUnprocessed(Configuration.EXTERNAL_APP_ID).fetch(page, length);
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
        List<LgZ> zList = LgZ.findUnprocessed(Configuration.EXTERNAL_APP_ID).fetch();
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

    public static void process(Integer zId) {
        if (zId == null) {
            unprocessed(1);
        }
        boolean stat = LgZ.process(Configuration.EXTERNAL_APP_ID, zId, "DONE");
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
        Integer totalPage = (int) (LgZ.count(startDate, endDate) / length) + 1;
        if (page > totalPage) {
            page = totalPage;
        }
        renderArgs.put("prevPage", page - 1);
        renderArgs.put("nextPage", page + 1);
        List<LgZ> zList = LgZ.find(startDate, endDate).fetch(page, length);
        renderArgs.put("startDate", startDate);
        renderArgs.put("endDate", endDate);
        renderArgs.put("page", page);
        renderArgs.put("totalPage", totalPage);
        renderArgs.put("data", zList);
        flash.put("backUrl", request.url);
        render();
    }

    public static void detail(Integer zId) {
        LgZ z;
        if (zId == null) {
            z = LgZ.getCurrentZ();
        } else {
            z = LgZ.findById(zId);
        }
        renderArgs.put("backUrl", flash.get("backUrl"));
        z.setRenderArgs(renderArgs.data);
        render();
    }

    public static void reprint(Integer zId) {
        LgZ z;
        if (zId == null) {
            z = LgZ.getCurrentZ();
        } else {
            z = LgZ.findById(zId);
        }
        z.print(true);
        list(zId, null, null);
    }

    public static void rotateZ() {
        LgZ currentZ = LgZ.rotateZ();
        currentZ.setRenderArgs(renderArgs.data);
        currentZ.print(false);
        flash.put("backUrl", Router.reverse("MenuController.AccountingMenu"));
        detail(currentZ.zId);
    }

    public static void print() {
        LgZ currentZ = LgZ.getCurrentZ();
        currentZ.setRenderArgs(renderArgs.data);
        currentZ.print(false);
        flash.put("backUrl", Router.reverse("MenuController.AccountingMenu"));
        detail(currentZ.zId);
    }
}
