/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.state.poll;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.glory.GloryDE50Device.GloryDE50DeviceStateApi;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.GloryDE50Error;
import devices.glory.state.GloryDE50Error.COUNTER_CLASS_ERROR_CODE;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.state.GloryDE50StateOperation;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.CANCELING;
import java.util.concurrent.atomic.AtomicBoolean;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class GloryDE50StatePoll extends GloryDE50StateOperation {

    final AtomicBoolean mustCancel = new AtomicBoolean(false);

    public GloryDE50StatePoll(GloryDE50DeviceStateApi api) {
        super(api);
    }

    abstract public GloryDE50StateAbstract poll(GloryDE50OperationResponse lastResponse);

    abstract public GloryDE50StateAbstract doCancel();

    GloryDE50StateAbstract sendGloryOperation(GloryDE50OperationInterface op) {
        if (op != null) {
            GloryDE50OperationResponse response = new GloryDE50OperationResponse();
            String error = api.sendGloryDE50Operation(op, false, response);
            if (error != null) {
                Logger.error("Error %s sending cmd : %s", error, op.getDescription());
                return new GloryDE50Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, error);
            }
        }
        return null;
    }

    @Override
    public DeviceStateInterface command(DeviceTaskAbstract task) {

        if (mustCancel.get()) {
            api.notifyListeners(CANCELING);
            Logger.debug("doCancel");
            GloryDE50StateAbstract ret = doCancel();
            if (ret != null) {
                return ret;
            }
            return new GloryDE50GotoNeutral(api);
        }

        GloryDE50OperationResponse response = new GloryDE50OperationResponse();
        String error = api.sendGloryDE50Operation(new devices.glory.operation.Sense(), false, response);
        if (error != null) {
            Logger.error("Error %s sending cmd : SENSE", error);
            return new GloryDE50Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, error);
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

    public boolean cancelDeposit() {
        mustCancel.set(true);
        return true;
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
     Logger.error("Error %s sending cmd : RemoteCancel", gloryStatus.getLastGloryDE50Error());
     return false;
     }
     return sense();
     }
     }
     */
}
