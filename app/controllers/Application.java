package controllers;

import devices.DeviceFactory;
import models.Configuration;
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
        try {
            //DeviceFactory.getPrinter().printAttributes();

            /*BillDepositController.FormData formData = new BillDepositController.FormData();
             formData.currency.currency = new Currency();
             formData.currency.currency.textId = "Pesos";
             renderArgs.put("formData", formData);*/
            /*EnvelopeDepositController.FormData formData = new EnvelopeDepositController.FormData();
             renderArgs.put("formData", formData);*/

            renderArgs.put("clientCode", Configuration.getClientDescription());

            /*            List<BillDeposit> depositList = BillDeposit.findAll();
             BillDeposit deposit = depositList.get(0);
             List<Bill> bl = deposit.getBillList();
             renderArgs.put("billData", bl);
             renderArgs.put("depositTotal", deposit.getTotal());
             renderArgs.put("deposit", deposit);
             renderArgs.put("envelopes", deposit.envelopes);*/
            // Print the ticket.
//            DeviceFactory.getPrinter().print("envelopeDeposit_start", renderArgs.data, 120);
//            DeviceFactory.getPrinter().print("envelopeDeposit_finish", renderArgs.data, 120);
//            DeviceFactory.getPrinter().print("billDeposit", renderArgs.data, 120);

        } catch (Throwable ex) {
            Logger.error("ERROR PRINTING : %s %s %s", ex, ex.getMessage(), ex.getCause());
        }
        //render( "Printer/billDeposit.html");
        redirect("Application.hardwareMenu");
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
