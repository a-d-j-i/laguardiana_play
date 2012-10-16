package controllers;

import devices.DeviceFactory;
import devices.IoBoard;
import devices.glory.manager.GloryManager;
import java.util.HashMap;
import java.util.Map;
import models.ModelFacade;
import models.db.LgSystemProperty;
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
    public static void printersMenu() {
        render();
    }

    public static void printTemplate() {
        try {
            DeviceFactory.getPrinter().printAttributes();
            Map args = new HashMap();
            BillDepositController.FormData formData = new BillDepositController.FormData();
            formData.currency.currency = new Currency();
            formData.currency.currency.textId = "Pesos";
            args.put("clientCode", LgSystemProperty.getProperty(LgSystemProperty.Types.CLIENT_CODE));
            args.put("formData", formData);
            args.put("depositTotal", 505);
            args.put("depositId", 12345);
            // Print the ticket.
//            DeviceFactory.getPrinter().print("envelopeDeposit_start", args);
//            DeviceFactory.getPrinter().print("envelopeDeposit_finish", args);
            DeviceFactory.getPrinter().print("billDeposit", args);

            /*renderArgs.put("testarg", "ADJI");
             DeviceFactory.getPrinter().print("test", renderArgs);*/
        } catch (Throwable ex) {
            Logger.error("ERROR PRINTING : %s %s %s", ex, ex.getMessage(), ex.getCause());
        }
        redirect("Application.otherMenu");
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
