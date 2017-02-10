package controllers;

import java.util.Date;
import java.util.List;
import models.ModelFacade;
import models.db.LgBag;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.Router;
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
        b.setRenderArgs(renderArgs.data);
        render();
    }

    public static void reprint(Integer page, @As("dd/MM/yyyy") Date startDate, @As("dd/MM/yyyy") Date endDate, Integer id) {
        LgBag b;
        if (id == null) {
            b = LgBag.getCurrentBag();
        } else {
            b = LgBag.findById(id);
        }
        b.print(true, false, 1);
        list(page, startDate, endDate);
    }

    public static void rotateBag() {
        ModelFacade.withdrawBag(false);
        MenuController.accountingMenu(null);
    }

    public static void print() {
        LgBag currentBag = LgBag.getCurrentBag();
        currentBag.print(false, false, 1);
        flash.put("backUrl", Router.reverse("MenuController.AccountingMenu"));
        detail(currentBag.bagId);
    }

}
