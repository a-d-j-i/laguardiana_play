/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.state;

import static devices.device.DeviceStatus.STATUS.CANCELING;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.MeiEbdsDevice.MeiEbdsTaskType;
import devices.mei.MeiEbdsDeviceStateApi;
import devices.mei.operation.MeiEbdsHostMsg;
import devices.mei.response.MeiEbdsAcceptorMsg;
import java.util.concurrent.TimeoutException;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsStateMain extends MeiEbdsStateAbstract {

    public MeiEbdsStateMain(MeiEbdsDeviceStateApi api) {
        super(api);
    }

    final boolean mustCancel = false;
    private boolean mustCount = false;
    private final MeiEbdsAcceptorMsg result = new MeiEbdsAcceptorMsg();

    private final MeiEbdsHostMsg currMsg = new MeiEbdsHostMsg();
    int retries = 0;

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        switch ((MeiEbdsTaskType) task.getType()) {
            case TASK_OPEN_PORT:
                DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
                task.setReturnValue(true);
                return new MeiEbdsOpenPort(api, open.getPort());
            case TASK_RESET:
                break;
            case TASK_STORE:
                break;
        }
        Logger.debug("ignoring task %s", task.toString());
        return null;
    }

    @Override
    public DeviceStateInterface step() {
        // execute tasks. TODO: Not allways.
        DeviceTaskAbstract deviceTask = api.poll();
        if (deviceTask != null) {
            Logger.debug("Got task : %s, executing", deviceTask);
            return deviceTask.execute(this);
        }
        // TODO: Must wait for message ack.
        if (mustCancel) {
            api.notifyListeners(CANCELING);
            Logger.debug("doCancel");
            mustCount = false;
            return this;
        }

        String err;
        try {
            err = api.getMessage(result);
            if (err != null) {
                return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
            }
            return processAcceptorMessage();
        } catch (TimeoutException ex) { //pool the machine.
            Logger.debug("Timeout waiting for device, retry");
            if (retries++ > 100) {
                return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, "Timeout reading from port");
            }
            if (mustCount) {
                currMsg.enableAllDenominations();
            } else {
                currMsg.disableAllDenominations();
            }
            err = api.sendMessage(currMsg);
            if (err != null) {
                return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
            }
        }
        return this;
    }

    private DeviceStateInterface processAcceptorMessage() {
        switch (result.getMessageType()) {
            case HostToAcceptor:
                return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, "got host to acceptor message type from acceptor");
            default:
                return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR,
                        String.format("unsupported message type %s", result.getMessageType().name()));
            case AcceptorToHost:
                Logger.debug("Received msg : %s", result.toString());
                break;
        }
        return null;
    }

}
