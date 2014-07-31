package devices.glory.state.poll;

import devices.device.DeviceMessageInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskMessage;
import devices.device.task.DeviceTaskReadTimeout;
import devices.glory.GloryDE50Device;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50ResponseWithData;
import devices.glory.state.GloryDE50StateAbstract;
import static devices.glory.status.GloryDE50Status.GloryDE50StatusType.CANCELING;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class GloryDE50StatePoll extends GloryDE50StateAbstract {

    public GloryDE50StatePoll(GloryDE50Device api) {
        super(api);
    }

    protected GloryDE50StateAbstract sendGloryOperation(GloryDE50OperationInterface op) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected String sendGloryDE50Operation(GloryDE50OperationInterface op, boolean debug, GloryDE50ResponseWithData response) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GloryDE50StateAbstract init() {
        senseOp = new devices.glory.operation.Sense();
        String err = api.writeOperation(senseOp, false);
        if (err != null) {
            Logger.debug("GloryDE50StatePoll error in init %s", err);
        }
        return null;
    }
    private GloryDE50OperationInterface senseOp = null;

    abstract public GloryDE50StateAbstract poll(GloryDE50ResponseWithData lastResponse);

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskMessage) {
            DeviceTaskMessage msgt = (DeviceTaskMessage) task;
            DeviceMessageInterface message = msgt.getMessage();
            if (message != senseOp) {
                Logger.error("This message %s don't correspond to the sense I sent", message.toString());
            } else {
                GloryDE50ResponseWithData resp = (GloryDE50ResponseWithData) msgt.getResponse();
                Logger.debug("Got response : %s to operation : %s", resp.toString(), resp.toString());
                Logger.debug(String.format("Sense D1Mode %s SR1 Mode : %s", resp.getD1Mode().name(), resp.getSr1Mode().name()));
                task.setReturnValue(true);
                return poll(resp);
            }
        } else if (task instanceof DeviceTaskReadTimeout) {
            senseOp = new devices.glory.operation.Sense();
            String err = api.writeOperation(senseOp, false);
            if (err != null) {
                Logger.debug("GloryDE50StatePoll error in writeOperation %s", err);
                task.setReturnValue(false);
            } else {
                task.setReturnValue(true);
            }
            return null;
        } else if (task instanceof DeviceTaskCancel) {
            Logger.debug("doCancel");
            task.setReturnValue(true);
            api.notifyListeners(CANCELING);
            return new GloryDE50StateGotoNeutral(api);
        }
        // if we want to support operations.
        return super.call(task);

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
