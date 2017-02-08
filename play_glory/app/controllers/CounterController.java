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

    protected static void wizardFixPageInt() {
        //refresh session cache
        if (ModelFacade.isLocked()) {
            if (!request.isAjax()) {
                CountController.counterError(null);
            }
        }
        if (request.isAjax()) {
            return;
        }
        String neededAction = ModelFacade.getNeededAction();
        String neededController = ModelFacade.getNeededController();
        if (neededAction == null || neededController == null) {
            if (!request.actionMethod.equalsIgnoreCase("start")) {
                Logger.debug("wizardFixPage Redirect Application.index");
                Application.index();
            }
        } else if (!(request.controller.equalsIgnoreCase(neededController))) {
            Logger.debug("wizardFixPage REDIRECT TO neededController %s : neededAction %s", neededController, neededAction);
            String dest = neededController + "." + neededAction;
            //boolean perm = Secure.checkPermission(dest, "GET");
            //if (!perm) {
            //}
            redirect(Router.getFullUrl(dest));
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
        if (!Configuration.isIgnoreIoBoard()) {
            final IoBoard ioBoard = ModelFacade.getIoBoard();
            if (ioBoard != null && ioBoard.getError() != null) {
                ierror = ioBoard.getError().toString();
            }
        }
        if (!Configuration.isIgnorePrinter() && !Configuration.isPrinterTest()) {
            pstatus = ModelFacade.getCurrentPrinter().getInternalState();
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
            Object ret[] = new Object[3];
            ret[0] = ModelFacade.isError();
            ret[1] = Configuration.getErrorStr();
            ret[2] = ModelFacade.getState();
            renderJSON(ret);
        } else {
            renderArgs.put("lockedByUser", ModelFacade.getLockedByUser());
            renderArgs.put("isError", ModelFacade.isError());
            renderArgs.put("errorStr", Configuration.getErrorStr());
            renderArgs.put("errorCode", ModelFacade.getState());
            render();
        }
    }
}
