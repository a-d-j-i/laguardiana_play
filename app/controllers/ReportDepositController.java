package controllers;

import java.util.Date;
import java.util.List;
import models.db.LgDeposit;
import play.Logger;
import play.data.binding.As;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With({Secure.class})
public class ReportDepositController extends Controller {

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
