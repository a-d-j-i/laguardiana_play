package controllers;

import devices.CounterFactory;
import devices.glory.manager.Manager;
import models.ModelFacade;
import models.TemplatePrinter;
import models.actions.UserAction;
import models.db.LgSystemProperty;
import play.Logger;
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

//    public static Object[] getCountingStatus() {
//        Manager.Status m = modelFacade.getExtraInfo();
//        Object[] o = new Object[3];
//        o[1] = modelFacade.getBillData();
//        o[2] = Messages.get("bill_deposit." + m.name().toLowerCase());
//        switch (modelFacade.getCurrentActionState()) {
//            case FINISH:
//                o[0] = "FINISH";
//                break;
//            case ERROR:
//                o[0] = "ERROR";
//                break;
//            case RUNNING:
//                Logger.debug("STATE : %s MANAGER STATE : %s", modelFacade.getCurrentActionState(), modelFacade.getExtraInfo().name());
//                switch (m) {
//                    case READY_TO_STORE:
//                        o[0] = "READY_TO_STORE";
//                        break;
//                    case ESCROW_FULL:
//                        o[0] = "ESCROW_FULL";
//                        break;
//                    case PUT_THE_BILLS_ON_THE_HOPER:
//                    case REMOVE_THE_BILLS_FROM_HOPER:
//                    case REMOVE_THE_BILLS_FROM_ESCROW:
//                    case REMOVE_REJECTED_BILLS:
//                    case CANCELING:
//                        o[0] = "IDLE";
//                        break;
//                    case IDLE:
//                        o[0] = "IDLE";
//                        o[2] = "";
//                        break;
//                    default:
//                        o[0] = modelFacade.getExtraInfo();
//                        break;
//                }
//                break;
//        }
//        return o;
//    }
    public static void counterError(Integer cmd) {
        Logger.debug("COUNTER ERROR");
        Manager.Status gstatus = null;
        Manager.ErrorDetail gerror = null;
        String status = "";
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        if (manager != null) {
            gstatus = manager.getStatus();
            gerror = manager.getErrorDetail();
        }
        if (cmd != null) {
            switch (cmd) {
                case 1:
                    //modelFacade.reset();
                    break;
                case 2:
                    //modelFacade.storingErrorReset();
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
//            switch (modelFacade.getCurrentMode()) {
//                case STORING_ERROR_RECOVERY:
//                case ERROR_RECOVERY:
//                    break;
//                case COUNTING:
//                case FILTERING:
//                case BILL_DEPOSIT:
//                case ENVELOPE_DEPOSIT:
//                    break;
//                default:
//                    Application.index();
//                    break;
//            }
//            if (modelFacade.getCurrentActionState() == ModelFacade.ActionState.FINISH) {
//                // Error Solved.
//                Application.index();
//            }
            render();
        }
    }

    public static void printTemplate() {
        TemplatePrinter.printTemplate("<h1>My First Heading</h1><p>My first paragraph.</p>");
        redirect("Application.index");
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
