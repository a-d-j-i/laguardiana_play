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

    @Before
    static void basicPropertiesAndFixWizard() throws Throwable {
        if (request.isAjax()) {
            return;
        }
        renderArgs.put("useHardwareKeyboard", isProperty("useHardwareKeyboard"));

        String neededController = ModelFacade.getNeededController();
        if (neededController == null) {
            return;
        }
        String neededAction = ModelFacade.getNeededAction();
        Logger.debug("Needed action : %s  Needed controller : %s", neededAction, neededController);
        if (neededController == null || neededAction == null) {
            return;
        }
        if (neededAction.equalsIgnoreCase("counterError")) {
            neededController = "Application";
        }
        if (!request.controller.equalsIgnoreCase(neededController)
                || !request.actionMethod.equalsIgnoreCase(neededAction)) {
            Logger.debug("basicPropertiesAndFixWizard REDIRECT TO neededController %s : neededAction %s", neededController, neededAction);
            redirect(Router.getFullUrl(neededController + "." + neededAction));
        }
    }

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

    public static void counterError(Integer cmd) {
        GloryManager.Status gstatus = null;
        String gerror = null;
        IoBoard.Status istatus = null;
        String ierror = null;

        final GloryManager.ControllerApi manager = DeviceFactory.getGloryManager();
        if (manager != null) {
            gstatus = manager.getStatus();
            gerror = manager.getStatus().getErrorDetail();
        }
        final IoBoard ioBoard = DeviceFactory.getIoBoard();
        if (ioBoard != null) {
            IoBoard.IoBoardStatus s = ioBoard.getStatus();
            istatus = s.status;
            ierror = s.error;
        }
        if (cmd != null) {
            switch (cmd) {
                case 1:
                    ModelFacade.errorReset();
                    break;
                case 2:
                    ModelFacade.storingErrorReset();
                    break;
            }
        }
        renderArgs.put("mstatus", ModelFacade.getState());
        renderArgs.put("merror", ModelFacade.getError());
        renderArgs.put("gstatus", gstatus);
        renderArgs.put("gerror", gerror);
        renderArgs.put("istatus", istatus);
        renderArgs.put("ierror", ierror);

        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = gstatus.name();
            if (gerror != null) {
                o[1] = gerror.toString();
            } else {
                o[1] = "";
            }
            renderJSON(o);
        } else {
            renderArgs.put("isError", ModelFacade.isError());
            render();
        }
    }

    public static void printTemplate() {
        try {
            DeviceFactory.getPrinter().printAttributes();
            Map args = new HashMap();
            BillDepositController.FormData formData = new BillDepositController.FormData();
            formData.currency.currency = new Currency();
            formData.currency.currency.textId = "Pesos";
            args.put("clientCode", getProperty("client_code"));
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
    static public String getProperty(String name) {
        return LgSystemProperty.getProperty(name);
    }

    @Util
    static public Boolean isProperty(String name) {
        return LgSystemProperty.isProperty(name);
    }

    @Util
    public static Boolean localError(String message, Object... args) {
        Logger.error(message, args);
        flash.error(message, args);
        return null;
    }
}
