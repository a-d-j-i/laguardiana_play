package controllers;

import devices.DeviceFactory;
import java.util.Date;
import java.util.List;
import java.util.Map;
import models.Bill;
import models.db.LgBillType;
import models.db.LgZ;
import play.data.binding.As;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Util;
import play.mvc.With;

@With({Secure.class})
public class ReportZController extends Controller {

    @Before
    public static void setFormat(String format) {
        if (format != null) {
            request.format = format;
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
        setRenderArgs(z);
        render();
    }

    public static void reprint(Integer zId) {
        LgZ z;
        if (zId == null) {
            z = LgZ.getCurrentZ();
        } else {
            z = LgZ.findById(zId);
        }
        setRenderArgs(z);
        renderArgs.put("reprint", "true");
        DeviceFactory.getPrinter().print("ReportZController/print.html", renderArgs.data, 200);
        list(zId, null, null);
    }

    public static void rotateZ() {
        LgZ currentZ = LgZ.rotateZ();
        setRenderArgs(currentZ);
        DeviceFactory.getPrinter().print("ReportZController/print.html", renderArgs.data, 200);
        flash.put("backUrl", Router.reverse("MenuController.AccountingMenu"));
        detail(currentZ.zId);
    }

    public static void print() {
        LgZ currentZ = LgZ.getCurrentZ();
        setRenderArgs(currentZ);
        DeviceFactory.getPrinter().print("ReportZController/print.html", renderArgs.data, 200);
        flash.put("backUrl", Router.reverse("MenuController.AccountingMenu"));
        detail(currentZ.zId);
    }

    @Util
    static public void setRenderArgs(LgZ z) {
        renderArgs.put("z", z);
        renderArgs.put("currentDate", new Date());
        renderArgs.put("totals", ReportController.getTotals(z.deposits));
    }
}
