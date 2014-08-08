package controllers;

import devices.printer.OSPrinter.PrinterStatus;
import machines.status.MachineStatus;
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
public class ErrorController extends Controller {

    static MachineStatus status;

    public static void onError() {
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
        if (request.isAjax()) {
            Object ret[] = new Object[3];
//            ret[ 0] = status.isError();
            ret[ 1] = Configuration.getErrorStr();
            ret[2] = status.getStateName();
            renderJSON(ret);
        } else {
//            renderArgs.put("isError", status.isError());
            renderArgs.put("errorStr", Configuration.getErrorStr());
            renderArgs.put("errorCode", status.getStateName());
            render();
        }
    }

    // TODO: Show machine status.
    public static void onStoringError() {
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
        if (request.isAjax()) {
            Object ret[] = new Object[3];
//            ret[ 0] = status.isError();
            ret[ 1] = Configuration.getErrorStr();
            ret[2] = status.getStateName();
            renderJSON(ret);
        } else {
//            renderArgs.put("isError", status.isError());
            renderArgs.put("errorStr", Configuration.getErrorStr());
            renderArgs.put("errorCode", status.getStateName());
            render();
        }
    }

    public static void reset() {

    }

    public static void storingErrorReset() {

    }

}
