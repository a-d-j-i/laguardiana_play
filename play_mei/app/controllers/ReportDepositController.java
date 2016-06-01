package controllers;

import java.util.Date;
import java.util.List;
import models.db.LgBag;
import models.db.LgDeposit;
import models.db.LgZ;
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

    public static void list(Integer page, @As("dd/MM/yyyy") Date startDate, @As("dd/MM/yyyy") Date endDate, Integer bagId, Integer zId) {
        if (endDate == null) {
            endDate = new Date();
        }
        if (page == null || page < 1) {
            page = 1;
        }
        int length = 3;
        long cnt = LgDeposit.count(startDate, endDate, bagId, zId);
        renderArgs.put("cnt", cnt);
        Integer totalPage = (int) (((cnt - 1) / length) + 1);
        if (page > totalPage) {
            page = totalPage;
        }
        renderArgs.put("prevPage", page - 1);
        renderArgs.put("nextPage", page + 1);

        List<LgDeposit> dList = LgDeposit.find(startDate, endDate, bagId, zId).fetch(page, length);
        renderArgs.put("startDate", startDate);
        renderArgs.put("endDate", endDate);
        if (bagId != null) {
            renderArgs.put("bag", LgBag.findById(bagId));
        }
        renderArgs.put("bagId", bagId);
        if (zId != null) {
            renderArgs.put("z", LgZ.findById(zId));
        }
        renderArgs.put("zId", zId);
        renderArgs.put("page", page);
        renderArgs.put("totalPage", totalPage);
        renderArgs.put("data", dList);
        flash.put("backUrl", request.url);
        render();
    }

    public static void detail(Integer id) {
        LgDeposit d = LgDeposit.findById(id);
        d.setRenderArgs(renderArgs.data);
        //Logger.debug("-------- BACK URL -> %s", flash.get("backUrl"));
        renderArgs.put("backUrl", flash.get("backUrl"));
        render(d.getDetailView());
    }

    public static void reprint(Integer page, @As("dd/MM/yyyy") Date startDate, @As("dd/MM/yyyy") Date endDate, Integer bagId, Integer zId, Integer id) {
        LgDeposit d = LgDeposit.findById(id);
        d.print(true);
        list(page, startDate, endDate, bagId, zId);
    }
}
