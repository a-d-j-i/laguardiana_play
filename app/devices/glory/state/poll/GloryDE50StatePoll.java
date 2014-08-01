package devices.glory.state.poll;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskReadTimeout;
import devices.glory.GloryDE50Device;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50Response;
import devices.glory.response.GloryDE50ResponseWithData;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.state.GloryDE50StateError;
import devices.glory.state.GloryDE50StateError.COUNTER_CLASS_ERROR_CODE;
import devices.glory.state.GloryDE50StateWaitForResponse;
import devices.glory.state.GloryDE50StateWaitForResponse.GloryDE50StateWaitForResponseCallback;
import devices.glory.task.GloryDE50TaskOperation;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class GloryDE50StatePoll extends GloryDE50StateAbstract {

    boolean debug = true;

    public GloryDE50StatePoll(GloryDE50Device api) {
        super(api);
    }

    @Override
    public GloryDE50StateAbstract init() {
        return sense();
    }

    abstract public GloryDE50StateAbstract poll(GloryDE50ResponseWithData senseResponse);

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskReadTimeout) {
            task.setReturnValue(true);
            return sense();
        } else {
            return super.call(task);
        }
    }

    protected GloryDE50StateAbstract sense() {
        return sendGloryOperation(new devices.glory.operation.Sense(), new GloryDE50StateWaitForResponseCallback() {

            @Override
            public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                Logger.debug("SENSE: %s", response.toString());
                return poll((GloryDE50ResponseWithData) response);
            }

        });
    }

    protected GloryDE50StateAbstract sendGloryOperation(GloryDE50OperationInterface operation, GloryDE50StateWaitForResponseCallback callBack) {
        GloryDE50TaskOperation opTask = new GloryDE50TaskOperation(operation, debug);
        String err = api.writeOperation(opTask, false);
        if (err != null) {
            Logger.debug("GloryDE50StatePoll error %s sending operation %s", err, operation.toString());
            opTask.setError(err);
            return new GloryDE50StateError(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, err);
        }
        return new GloryDE50StateWaitForResponse(api, opTask, callBack);
    }

    protected GloryDE50StateAbstract sendGloryOperation(GloryDE50OperationInterface operation) {
        return sendGloryOperation(operation, new GloryDE50StateWaitForResponseCallback() {

            public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                return sense();
            }
        });
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
    @Override
    public String toString() {
        return "GloryDE50StatePoll{" + "debug=" + debug + '}';
    }

}
