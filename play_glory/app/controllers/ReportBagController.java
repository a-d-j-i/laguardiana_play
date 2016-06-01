package controllers;

import java.util.Date;
import java.util.List;
import models.Configuration;
import models.ModelFacade;
import models.db.LgBag;
import play.data.binding.As;
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
        long cnt = LgBag.count(startDate, endDate);
        renderArgs.put("cnt", cnt);
        Integer totalPage = (int) ((cnt / length) + 1);
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

    public static void detail(Integer id) {
        LgBag b;
        if (id == null) {
            b = LgBag.getCurrentBag();
        } else {
            b = LgBag.findById(id);
        }
        renderArgs.put("backUrl", flash.get("backUrl"));
        setRenderArgs(b);
        render();
    }

    public static void reprint(Integer page, @As("dd/MM/yyyy") Date startDate, @As("dd/MM/yyyy") Date endDate, Integer id) {
        LgBag b;
        if (id == null) {
            b = LgBag.getCurrentBag();
        } else {
            b = LgBag.findById(id);
        }
        setRenderArgs(b);
        renderArgs.put("reprint", "true");
        ModelFacade.print("ReportBagController/print.html", renderArgs.data, Configuration.getPrintWidth(), Configuration.getBagPrintLen());
        list(page, startDate, endDate);
    }

    public static void rotateBag() {
        LgBag.withdrawBag(false);
        MenuController.accountingMenu(null);
    }

    public static void print() {
        LgBag currentBag = LgBag.getCurrentBag();
        setRenderArgs(currentBag);
        ModelFacade.print("ReportBagController/print.html", renderArgs.data, Configuration.getPrintWidth(), Configuration.getBagPrintLen());
        flash.put("backUrl", Router.reverse("MenuController.AccountingMenu"));
        detail(currentBag.bagId);
    }

    @Util
    static public void setRenderArgs(LgBag b) {
        renderArgs.put("bag", b);
        renderArgs.put("currentDate", new Date());
        renderArgs.put("ticketFooter", Configuration.getTicketFooter());
        renderArgs.put("ticketHeader", Configuration.getTicketHeader());
        renderArgs.put("totals", b.getTotals());
    }
}