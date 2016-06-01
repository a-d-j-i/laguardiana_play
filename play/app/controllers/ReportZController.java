package controllers;

import java.util.Date;
import java.util.List;
import models.Configuration;
import models.ModelFacade;
import models.db.LgZ;
import play.data.binding.As;
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
        long cnt = LgZ.count(startDate, endDate);
        renderArgs.put("cnt", cnt);
        Integer totalPage = (int) (((cnt - 1) / length) + 1);
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

    public static void detail(Integer id) {
        LgZ z;
        if (id == null) {
            z = LgZ.getCurrentZ();
        } else {
            z = LgZ.findById(id);
        }
        renderArgs.put("backUrl", flash.get("backUrl"));
        setRenderArgs(z);
        render();
    }

    public static void reprint(Integer page, @As("dd/MM/yyyy") Date startDate, @As("dd/MM/yyyy") Date endDate, Integer id) {
        LgZ z;
        if (id == null) {
            z = LgZ.getCurrentZ();
        } else {
            z = LgZ.findById(id);
        }
        setRenderArgs(z);
        renderArgs.put("reprint", "true");
        ModelFacade.print("ReportZController/print.html", renderArgs.data, Configuration.getPrintWidth(), Configuration.getZPrintLen());
        list(page, startDate, endDate);
    }

    public static void rotateZ() {
        LgZ currentZ = LgZ.rotateZ();
        setRenderArgs(currentZ);
        ModelFacade.print("ReportZController/print.html", renderArgs.data, Configuration.getPrintWidth(), Configuration.getZPrintLen());
        renderArgs.put("copy", "copy");
        ModelFacade.print("ReportZController/print.html", renderArgs.data, Configuration.getPrintWidth(), Configuration.getZPrintLen());
        flash.put("backUrl", Router.reverse("MenuController.AccountingMenu"));
        detail(currentZ.zId);
    }

    public static void print() {
        LgZ currentZ = LgZ.getCurrentZ();
        setRenderArgs(currentZ);
        ModelFacade.print("ReportZController/print.html", renderArgs.data, Configuration.getPrintWidth(), Configuration.getZPrintLen());
        flash.put("backUrl", Router.reverse("MenuController.AccountingMenu"));
        detail(currentZ.zId);
    }

    @Util
    static public void setRenderArgs(LgZ z) {
        renderArgs.put("z", z);
        renderArgs.put("currentDate", new Date());
        renderArgs.put("ticketFooter", Configuration.getTicketFooter());
        renderArgs.put("ticketHeader", Configuration.getTicketHeader());
        renderArgs.put("totals", z.getTotals());
    }
}
