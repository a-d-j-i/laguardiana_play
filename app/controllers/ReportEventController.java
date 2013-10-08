package controllers;

import java.util.Date;
import java.util.List;
import models.db.LgEvent;
import play.data.binding.As;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

@With({Secure.class})
public class ReportEventController extends Controller {

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
        long cnt = LgEvent.count(startDate, endDate);
        renderArgs.put("cnt", cnt);
        Integer totalPage = (int) ((cnt / length) + 1);
        if (page > totalPage) {
            page = totalPage;
        }
        renderArgs.put("prevPage", page - 1);
        renderArgs.put("nextPage", page + 1);
        List<LgEvent> eventList = LgEvent.find(startDate, endDate).fetch(page, length);
        renderArgs.put("startDate", startDate);
        renderArgs.put("endDate", endDate);
        renderArgs.put("page", page);
        renderArgs.put("totalPage", totalPage);
        renderArgs.put("data", eventList);
        flash.put("backUrl", request.url);
        render();
    }

    public static void detail(Integer id) {
        LgEvent event = LgEvent.findById(id);
        renderArgs.put("backUrl", flash.get("backUrl"));
        setRenderArgs(event);
        render();
    }

    @Util
    static public void setRenderArgs(LgEvent event) {
        renderArgs.put("event", event);
        renderArgs.put("currentDate", new Date());
    }
}
