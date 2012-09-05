package controllers;

import models.ModelFacade;
import models.TemplatePrinter;
import models.db.LgSystemProperty;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Util;
import play.mvc.With;

@With(Secure.class)
public class Application extends Controller {

    @Before
    static void wizardFixPage() throws Throwable {
        if (request.isAjax()) {
            return;
        }
        String neededController;
        switch (modelFacade.getCurrentMode()) {
            case COUNTING:
                neededController = "CountController";
                break;
            case FILTERING:
                neededController = "FilterController";
                break;
            case BILL_DEPOSIT:
                neededController = "BillDepositController";
                break;
            case ENVELOPE_DEPOSIT:
                neededController = "EnvelopeDepositController";
                break;
            default:
                return;
        }
        switch (modelFacade.getCurrentStep()) {
            case NONE: // really don't happend.
                if (!request.actionMethod.equalsIgnoreCase("start")) {
                    redirect(Router.getFullUrl(neededController + ".start"));
                }
                break;
            case RUNNING:
                if (!request.actionMethod.equalsIgnoreCase("mainLoop")
                        && !request.actionMethod.equalsIgnoreCase("cancel")
                        && !request.actionMethod.equalsIgnoreCase("accept")) {
                    redirect(Router.getFullUrl(neededController + ".mainLoop"));
                }
                break;
            case FINISH:
                if (!request.actionMethod.equalsIgnoreCase("finish")) {
                    redirect(Router.getFullUrl(neededController + ".finish"));
                }
                break;
            case ERROR:
                error("APPLICATION ERROR !!!");
                break;
            case RESERVED:
            default: // do nothing
                break;
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
    static ModelFacade modelFacade = ModelFacade.get();

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
