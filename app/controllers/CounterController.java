/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import devices.ioboard.IoBoard;
import devices.glory.manager.ManagerInterface;
import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.printer.Printer.PrinterStatus;
import models.Configuration;
import models.ModelFacade;
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
public class CounterController extends Controller {

    @Before
    static void basicPropertiesAndFixWizard() throws Throwable {
        if (request.isAjax()) {
            return;
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
            neededController = "CounterController";
        }
        if (!request.controller.equalsIgnoreCase(neededController)
                || !request.actionMethod.equalsIgnoreCase(neededAction)) {
            Logger.debug("basicPropertiesAndFixWizard REDIRECT TO neededController %s : neededAction %s", neededController, neededAction);
            redirect(Router.getFullUrl(neededController + "." + neededAction));
        }
    }

    public static void counterError(Integer cmd) {
        ManagerStatus gstatus = null;
        String gerror = null;
        PrinterStatus pstatus = null;
        String ierror = null;

        final ManagerInterface manager = ModelFacade.getGloryManager();
        if (manager != null) {
            gstatus = manager.getStatus();
            gerror = manager.getStatus().toString();
        }
        if (!Configuration.isIoBoardIgnore()) {
            final IoBoard ioBoard = ModelFacade.getIoBoard();
            if (ioBoard != null && ioBoard.getError() != null) {
                ierror = ioBoard.getError().toString();
            }
        }
        if (!Configuration.isIgnorePrinter() && !Configuration.isPrinterTest()) {
            pstatus = ModelFacade.getPrinter().getInternalState();
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
        if (request.isAjax()) {
            renderJSON(ModelFacade.isError());
        } else {
            renderArgs.put("mstatus", ModelFacade.getState());
            renderArgs.put("merror", ModelFacade.getError());
            renderArgs.put("pstatus", pstatus);
            renderArgs.put("gstatus", gstatus);
            renderArgs.put("gerror", gerror);
            renderArgs.put("ierror", ierror);
            renderArgs.put("isError", ModelFacade.isError());
            render();
        }
    }
}
