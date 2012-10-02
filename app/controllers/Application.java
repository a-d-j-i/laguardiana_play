package controllers;

import devices.DeviceFactory;
import devices.IoBoard;
import devices.glory.manager.GloryManager;
import models.ModelFacade;
import models.db.LgSystemProperty;
import play.Logger;
import play.Play;
import play.mvc.*;

@With({Secure.class})
public class Application extends Controller {

    static int defaultTimeout = 60;

    @Before
    static void basicPropertiesAndFixWizard() throws Throwable {
        if (request.isAjax()) {
            return;
        }
        renderArgs.put("useHardwareKeyboard", isProperty("useHardwareKeyboard"));
        try {
            defaultTimeout = Integer.parseInt(Play.configuration.getProperty("timer.timeout"));
        } catch (NumberFormatException e) {
            Logger.debug("Error parsing timer.timeout %s", e.getMessage());
        }

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
        GloryManager.ErrorDetail gerror = null;
        IoBoard.Status istatus = null;
        String ierror = null;

        final GloryManager.ControllerApi manager = DeviceFactory.getGloryManager();
        if (manager != null) {
            gstatus = manager.getStatus();
            gerror = manager.getErrorDetail();
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
                    ModelFacade.reset();
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
            renderArgs.put("testarg", "ADJI");
            DeviceFactory.getPrinter().print("test", renderArgs);
        } catch (Throwable ex) {
            Logger.error(ex.getMessage());
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
