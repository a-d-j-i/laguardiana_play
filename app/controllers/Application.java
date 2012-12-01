package controllers;

import devices.DeviceFactory;
import java.util.List;
import models.Bill;
import models.BillDeposit;
import models.Configuration;
import models.lov.Currency;
import play.Logger;
import play.mvc.*;

@With({Secure.class})
public class Application extends Controller {

    public static void index() {
        mainMenu();
    }

    public static void mainMenu() {
        render();
    }

    public static void otherMenu() {
        render();
    }

    public static void hardwareMenu() {
        render();
    }

    public static void printTemplate() {
        render();
    }

    public static void listPrinters() {
        renderArgs.put("printers", DeviceFactory.getPrinter().printers.values());
        render();
    }

    @Util
    public static Boolean localError(String message, Object... args) {
        Logger.error(message, args);
        flash.error(message, args);
        return null;
    }
}
