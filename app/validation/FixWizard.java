/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import controllers.Application;
import models.ModelFacade;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;

/**
 *
 * @author adji
 */
public class FixWizard extends Controller {

    static ModelFacade modelFacade = ModelFacade.get();

    @Before
    static void wizardFixPage() throws Throwable {
        if (request.isAjax()) {
            return;
        }
        Logger.debug("wizardFixPage mode %s : step %s", modelFacade.getCurrentMode().name(), modelFacade.getCurrentStep().name());
        String neededController;
        switch (modelFacade.getCurrentMode()) {
            case ERROR_RECOVERY:
            case STORING_ERROR_RECOVERY:
                if (!request.controller.equalsIgnoreCase("Application") || !request.actionMethod.equalsIgnoreCase("counterError")) {
                    Application.counterError(null);
                }
                return;
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
                if (!request.actionMethod.equalsIgnoreCase("counterError")) {
                    Application.counterError(null);
                }
                break;
            case RESERVED:
            default: // do nothing
                break;
        }
    }
}