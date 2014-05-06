/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50StateMachineApi;
import static devices.glory.GloryDE50Device.STATUS.CANCELING;
import devices.glory.response.GloryDE50Response;
import devices.glory.status.GloryDE50DeviceErrorEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class GloryDE50StatePoll extends GloryDE50StateAbstract {

    final AtomicBoolean mustCancel = new AtomicBoolean(false);

    public GloryDE50StatePoll(GloryDE50StateMachineApi api) {
        super(api);
    }

    abstract public GloryDE50StateAbstract poll(GloryDE50Response lastResponse);
    abstract public GloryDE50StateAbstract doCancel();

    @Override
    public GloryDE50StateAbstract step() {
        if (mustCancel.get()) {
            notifyListeners(CANCELING);
            return doCancel();
        }

        GloryDE50Response response = api.sendGloryOperation(new devices.glory.command.Sense());
        if (response.isError()) {
            String error = response.getError();
            Logger.error("Error %s sending cmd : SENSE", error);
            return new Error(api, GloryDE50DeviceErrorEvent.ERROR_CODE.GLORY_MANAGER_ERROR, error);
        }
        Logger.debug(String.format("Sense D1Mode %s SR1 Mode : %s", response.getD1Mode().name(), response.getSr1Mode().name()));
        GloryDE50StateAbstract ret = poll(response);
        if (ret != this) {
            return ret;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
        return null;
    }

    /*
     protected boolean sendRemoteCancel() {
     // Under this conditions the remoteCancel command fails.
     if (gloryStatus.isRejectBillPresent()) {
     setState(ManagerInterface.State.REMOVE_REJECTED_BILLS);
     return false;
     }
     if (gloryStatus.isHopperBillPresent()) {
     setState(ManagerInterface.State.REMOVE_THE_BILLS_FROM_HOPER);
     return false;
     }
     switch (gloryStatus.getSr1Mode()) {
     case counting:
     case storing_start_request:
     return false;
     default:
     if (!sendGCommand(new devices.glory.command.RemoteCancel())) {
     Logger.error("Error %s sending cmd : RemoteCancel", gloryStatus.getLastError());
     return false;
     }
     return sense();
     }
     }
     */
}
