package controllers;

import devices.DeviceFactory;
import devices.Printer;
import devices.glory.manager.GloryManager;
import java.io.IOException;
import java.util.logging.Level;
import models.ModelFacade;
import models.actions.UserAction;
import models.db.LgBag;
import models.db.LgSystemProperty;
import play.Logger;
import play.libs.F;
import play.libs.F.Tuple;
import play.mvc.*;

@With({Secure.class})
public class Application extends Controller {

    @Before
    static void basicPropertiesAndFixWizard() throws Throwable {
        Tuple<UserAction, Boolean> userActionTuple = ModelFacade.getCurrentUserAction();

        if (request.isAjax()) {
            return;
        }
        renderArgs.put("useHardwareKeyboard", isProperty("useHardwareKeyboard"));

        if (userActionTuple._1 == null) {
            return;
        }
        String neededController = userActionTuple._1.getNeededController();
        String neededAction = userActionTuple._1.getControllerAction();
        Logger.debug("Needed action : %s  Needed controller : %s", neededAction, neededController);
        if (neededController == null || neededAction == null) {
            return;
        }
        if (neededAction.equalsIgnoreCase("counterError")) {
            neededController = "Application";
        }
        if (!request.controller.equalsIgnoreCase(neededController)
                || !request.actionMethod.equalsIgnoreCase(neededAction)) {
            Logger.debug("wizardFixPage REDIRECT TO neededController %s : neededAction %s", neededController, neededAction);
            redirect(Router.getFullUrl(neededController + "." + neededAction));
        }
    }

    @Util
    static public <T> T getCurrentAction() {
        Tuple<UserAction, Boolean> userActionTuple = ModelFacade.getCurrentUserAction();
        return (T) userActionTuple._1;
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
        String status = "";
        final GloryManager.ControllerApi manager = DeviceFactory.getGloryManager();
        if (manager != null) {
            gstatus = manager.getStatus();
            gerror = manager.getErrorDetail();
        }
        F.Tuple<UserAction, Boolean> userActionTuple = ModelFacade.getCurrentUserAction();
        if (userActionTuple._1 != null) {
            status = userActionTuple._1.error;
        }
        if (cmd != null) {
            if (userActionTuple._1 != null) {
                userActionTuple._1.finishAction();
            }
            switch (cmd) {
                case 1:
                    manager.reset(null);
                    break;
                case 2:
                    manager.storingErrorReset(null);
                    break;
            }
        }

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
            renderArgs.put("status", status);
            renderArgs.put("gerror", gstatus);
            renderArgs.put("gstatus", gerror);
            userActionTuple = ModelFacade.getCurrentUserAction();

            if (userActionTuple._1 == null && gstatus == GloryManager.Status.IDLE) {
                // Error Solved.
                Application.index();
            }
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
