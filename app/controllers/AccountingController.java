package controllers;

import devices.DeviceFactory;
import models.db.LgBag;
import models.db.LgZ;
import play.Logger;
import play.mvc.Controller;

public class AccountingController extends Controller {

    public static void index() {
        mainMenu();
    }

    public static void mainMenu() {
        render();
    }

    public static void rotateBag() {
        LgBag.rotateBag(false);
        index();
    }

    public static void currentBagTotals() {
        LgBag currentBag = LgBag.getCurrentBag();
        renderArgs.put("bag", currentBag);
        if (currentBag != null) {
            renderArgs.put("totals", currentBag.getTotals());
        }
        try {
            DeviceFactory.getPrinter().print("currentBagTotals", renderArgs.data, 120);
        } catch (Throwable ex) {
            Logger.error("ERROR PRINTING : %s %s %s", ex, ex.getMessage(), ex.getCause());
        }
        render();
    }

    public static void currentZTotals() {
        LgZ currentZ = LgZ.getCurrentZ();
        renderArgs.put("z", currentZ);
        if (currentZ != null) {
            renderArgs.put("totals", currentZ.getTotals());
        }
        try {
            DeviceFactory.getPrinter().print("currentZTotals", renderArgs.data, 120);
        } catch (Throwable ex) {
            Logger.error("ERROR PRINTING : %s %s %s", ex, ex.getMessage(), ex.getCause());
        }
        render();
    }

    public static void rotateZ() {
        LgZ currentZ = LgZ.rotateZ();
        renderArgs.put("z", currentZ);
        if (currentZ != null) {
            renderArgs.put("totals", currentZ.getTotals());
        }
        renderTemplate("AccountingController/currentZTotals.html", renderArgs);
    }
}
