/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.state;

import devices.device.DeviceStatus;
import static devices.device.DeviceStatus.STATUS.CANCELING;
import devices.device.task.DeviceTaskInterface;
import devices.mei.MeiEbdsDeviceStateApi;
import devices.mei.operation.MeiEbdsHostMsg;
import devices.mei.response.MeiEbdsAcceptorMsg;
import java.util.concurrent.atomic.AtomicBoolean;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsStateMain extends MeiEbdsStateOperation {

    final AtomicBoolean mustCancel = new AtomicBoolean(false);

    public MeiEbdsStateMain(MeiEbdsDeviceStateApi api) {
        super(api);
    }

    public MeiEbdsStateOperation poll(MeiEbdsAcceptorMsg lastResponse) {
        return this;
    }

    public MeiEbdsStateOperation doCancel() {
        return this;
    }

    protected void notifyListeners(DeviceStatus.STATUS status) {
        api.notifyListeners(status);
    }

    private final MeiEbdsAcceptorMsg result = new MeiEbdsAcceptorMsg();
    private final MeiEbdsHostMsg msg = new MeiEbdsHostMsg();

    @Override
    public MeiEbdsStateOperation step() {
        // execute tasks. TODO: Not allways.
        DeviceTaskInterface deviceTask = api.peek();
        if (deviceTask != null) {
            Logger.debug("Got task : %s, executing", deviceTask);
            return (MeiEbdsStateOperation) deviceTask.execute(this);
        }
        msg.enableAllDenominations();
        MeiEbdsStateOperation nextStep = api.exchangeMessage(msg, result);
        if (nextStep == null) {
            Logger.debug("MEI received msg : %s", result.toString());
            return this;
        }
        if (nextStep.isError()) {
            return nextStep;
        }
        if (mustCancel.get()) {
            notifyListeners(CANCELING);
            Logger.debug("doCancel");
            MeiEbdsStateOperation ret = doCancel();
            if (ret != null) {
                return ret;
            }
            // TODO: Right state
            return this;
        }

        /*        MeiEbdsOperationResponse response = api.sendOperation();
         if (response.isError()) {
         String error = response.getError();
         Logger.error("Error %s sending cmd : SENSE", error);
         return new Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, error);
         }
         Logger.debug(String.format("Sense D1Mode %s SR1 Mode : %s", response.getD1Mode().name(), response.getSr1Mode().name()));
         MeiEbdsStateAbstract ret = poll(response);
         if (ret != this) {
         return ret;
         }
         try {
         Thread.sleep(1000);
         } catch (InterruptedException ex) {
         }*/
        return null;
    }

    @Override
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
     Logger.error("Error %s sending cmd : RemoteCancel", gloryStatus.getLastError());
     return false;
     }
     return sense();
     }
     }
     */
}
