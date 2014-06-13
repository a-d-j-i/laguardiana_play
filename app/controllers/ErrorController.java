/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import devices.printer.OSPrinter.PrinterStatus;
import models.Configuration;
import models.ModelFacade;
import models.facade.status.ModelFacadeStateStatus;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.With;

/**
 * Base class for all the controllers that use the counter to deposit.
 *
 * @author adji
 */
@With({Secure.class})
public class ErrorController extends Controller {

    static ModelFacadeStateStatus status;

    @Before
    static void basicPropertiesAndFixWizard() throws Throwable {
        if (request.isAjax()) {
            return;
        }
        status = ModelFacade.getStateStatus();
        String neededController = status.getNeededController();
        if (neededController == null) {
            return;
        }
        String neededAction = status.getNeededAction();
        Logger.debug("Needed action : %s  Needed controller : %s", neededAction, neededController);
        if (neededAction == null) {
            return;
        }
        if (neededAction.equalsIgnoreCase("onError")) {
            neededController = "CounterController";
        }
        if (!request.controller.equalsIgnoreCase(neededController)
                || !request.actionMethod.equalsIgnoreCase(neededAction)) {
            Logger.debug("basicPropertiesAndFixWizard REDIRECT TO neededController %s : neededAction %s", neededController, neededAction);
            redirect(Router.getFullUrl(neededController + "." + neededAction));
        }
    }

    // TODO: Show machine status.
    public static void onError(Integer cmd) {
        String gerror = null;
        PrinterStatus pstatus = null;
        String ierror = null;

        if (!Configuration.isIgnoreIoBoard()) {
            /*            final IoBoard ioBoard = ModelFacade.getIoBoard();
             if (ioBoard != null && ioBoard.getError() != null) {
             ierror = ioBoard.getError().toString();
             }*/
        }
        if (!Configuration.isIgnorePrinter() && !Configuration.isPrinterTest()) {
            pstatus = ModelFacade.getCurrentPrinter().getInternalState();
        }
        if (cmd != null) {
            ModelFacade.finishAction();
        }
        if (request.isAjax()) {
            Object ret[] = new Object[3];
            ret[ 0] = status.isError();
            ret[ 1] = Configuration.getErrorStr();
            ret[2] = status.getState();
            renderJSON(ret);
        } else {
            renderArgs.put("isError", status.isError());
            renderArgs.put("errorStr", Configuration.getErrorStr());
            renderArgs.put("errorCode", status.getState());
            render();
        }
    }

    // TODO: Show machine status.
    public static void onStoringError(Integer cmd) {
        String gerror = null;
        PrinterStatus pstatus = null;
        String ierror = null;

        if (!Configuration.isIgnoreIoBoard()) {
            /*            final IoBoard ioBoard = ModelFacade.getIoBoard();
             if (ioBoard != null && ioBoard.getError() != null) {
             ierror = ioBoard.getError().toString();
             }*/
        }
        if (!Configuration.isIgnorePrinter() && !Configuration.isPrinterTest()) {
            pstatus = ModelFacade.getCurrentPrinter().getInternalState();
        }
        if (cmd != null) {
            ModelFacade.finishAction();
        }
        if (request.isAjax()) {
            Object ret[] = new Object[3];
            ret[ 0] = status.isError();
            ret[ 1] = Configuration.getErrorStr();
            ret[2] = status.getState();
            renderJSON(ret);
        } else {
            renderArgs.put("isError", status.isError());
            renderArgs.put("errorStr", Configuration.getErrorStr());
            renderArgs.put("errorCode", status.getState());
            render();
        }
    }

}
