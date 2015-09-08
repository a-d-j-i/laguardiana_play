package controllers;

import machines.status.MachineStatus;
import machines.status.MachineStatusError;
import models.Configuration;
import models.ModelFacade;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Base class for all the controllers that use the counter to deposit.
 *
 * @author adji
 */
@With({Secure.class})
public class ErrorController extends Controller {

    static MachineStatus status;

    @Before
    static void basicPropertiesAndFixWizard() throws Throwable {
        status = ModelFacade.getCurrentStatus();
        if (request.isAjax()) {
            return;
        }
    }

    public static void onError() {
        String gerror = null;
        String pstatus = null;
        String ierror = null;

        if (!Configuration.isIgnoreIoBoard()) {
            /*            final IoBoard ioBoard = ModelFacade.getIoBoard();
             if (ioBoard != null && ioBoard.getError() != null) {
             ierror = ioBoard.getError().toString();
             }*/
        }
        if (!Configuration.isIgnorePrinter() && !Configuration.isPrinterTest()) {
            pstatus = ModelFacade.getPrinterState();
        }
        boolean isError = true;
        if (status instanceof MachineStatusError) {
            MachineStatusError mse = (MachineStatusError) status;
        }
        if (request.isAjax()) {
            Object ret[] = new Object[3];
            ret[0] = isError;
            ret[1] = Configuration.getErrorStr();
            ret[2] = status.getStateName();
            renderJSON(ret);
        } else {
            renderArgs.put("lockedByUser", ModelFacade.getLockedByUser());
            renderArgs.put("isError", isError);
            renderArgs.put("errorStr", Configuration.getErrorStr());
            renderArgs.put("errorCode", status.getStateName());
            render();
        }
    }

    // TODO: Show machine status.
    public static void onStoringError() {
        String gerror = null;
        String pstatus = null;
        String ierror = null;

        if (!Configuration.isIgnoreIoBoard()) {
            /*            final IoBoard ioBoard = ModelFacade.getIoBoard();
             if (ioBoard != null && ioBoard.getError() != null) {
             ierror = ioBoard.getError().toString();
             }*/
        }
        if (!Configuration.isIgnorePrinter() && !Configuration.isPrinterTest()) {
            pstatus = ModelFacade.getPrinterState();
        }
        boolean isError = true;
        if (status instanceof MachineStatusError) {
            MachineStatusError mse = (MachineStatusError) status;
        }
        if (request.isAjax()) {
            Object ret[] = new Object[3];
            ret[0] = isError;
            ret[1] = Configuration.getErrorStr();
            ret[2] = status.getStateName();
            renderJSON(ret);
        } else {
            renderArgs.put("isError", isError);
            renderArgs.put("errorStr", Configuration.getErrorStr());
            renderArgs.put("errorCode", status.getStateName());
            render();
        }
    }

    public static void reset() {
        ModelFacade.errorReset();
        onError();
    }

    public static void storingErrorReset() {
        ModelFacade.storingErrorReset();
        onError();
    }

}
