package controllers;

import devices.DeviceFactory;
import java.util.Date;
import java.util.List;
import java.util.Map;
import models.Bill;
import models.db.LgBag;
import models.db.LgBillType;
import play.data.binding.As;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Util;
import play.mvc.With;

@With({Secure.class})
public class ReportBagController extends Controller {

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
        setRenderArgs(b);
        render();
    }

    public static void reprint(Integer bId) {
        LgBag b;
        if (bId == null) {
            b = LgBag.getCurrentBag();
        } else {
            b = LgBag.findById(bId);
        }
        setRenderArgs(b);
        renderArgs.put("reprint", "true");
        DeviceFactory.getPrinter().print("ReportBagController/print.html", renderArgs.data, 200);
        list(bId, null, null);
    }

    public static void rotateBag() {
        LgBag.rotateBag(false);
        MenuController.accountingMenu(null);
    }

    public static void print() {
        LgBag currentBag = LgBag.getCurrentBag();
        setRenderArgs(currentBag);
        DeviceFactory.getPrinter().print("ReportBagController/print.html", renderArgs.data, 200);
        flash.put("backUrl", Router.reverse("MenuController.AccountingMenu"));
        detail(currentBag.bagId);
    }

    @Util
    static public void setRenderArgs(LgBag b) {
        renderArgs.put("bag", b);
        renderArgs.put("currentDate", new Date());
        renderArgs.put("totals", ReportController.getTotals(b.deposits));
    }
}
