package controllers;

import models.db.LgBag;
import models.db.LgZ;
import play.mvc.Controller;

public class AccountingController extends Controller {

    public static void rotateBag() {
        LgBag.rotateBag(false);
        MenuController.accountingMenu(null);
    }

    public static void currentBagTotals() {
        LgBag currentBag = LgBag.getCurrentBag();
        currentBag.print();
        currentBag.setRenderArgs(renderArgs.data);
        render();
    }

    public static void currentZTotals() {
        LgZ currentZ = LgZ.getCurrentZ();
        currentZ.print();
        currentZ.setRenderArgs(renderArgs.data);
        render();
    }

    public static void rotateZ() {
        LgZ currentZ = LgZ.rotateZ();
        currentZ.setRenderArgs(renderArgs.data);
        currentZ.print();
        renderTemplate("AccountingController/currentZTotals.html", renderArgs);
    }
}
