/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.MeiEbdsDevice;
import devices.mei.MeiEbdsDevice.MeiEbdsDeviceStateApi;
import devices.mei.MeiEbdsDevice.MeiEbdsTaskType;
import devices.mei.operation.MeiEbdsHostMsg;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.mei.response.MeiEbdsAcceptorMsgError;
import devices.mei.response.MeiEbdsAcceptorMsgInterface;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatus.MeiEbdsStatusType;
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

    private boolean mustCancel = false;
    private boolean mustCount = false;
    private boolean mustStore = false;
    private boolean mustReject = false;
    private final MeiEbdsAcceptorMsgAck result = new MeiEbdsAcceptorMsgAck();

    private final MeiEbdsHostMsg hostPollMsg = new MeiEbdsHostMsg();
    int retries = 0;

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        Logger.debug("----------------> Received a task call : %s", task.getType().name());
        switch ((MeiEbdsTaskType) task.getType()) {
            case TASK_OPEN_PORT:
                DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
                task.setReturnValue(true);
                return new MeiEbdsOpenPort(api, open.getPort());
            case TASK_RESET:
                break;
            case TASK_COUNT:
                mustCount = true;
                break;
            case TASK_STORE:
                mustStore = true;
                break;
            case TASK_REJECT:
                mustReject = true;
                break;
            case TASK_CANCEL:
                mustCancel = true;
                break;
        }
        Logger.debug("ignoring task %s", task.toString());
        return null;
    }

    // send the first message
    @Override
    public DeviceStateInterface init() {
        String err = sendMessage(hostPollMsg);
        if (err != null) {
            return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
        }
        return this;
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
            api.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.CANCELING));
            Logger.debug("doCancel");
            mustCount = false;
            return this;
        }

        try {
            MeiEbdsAcceptorMsgInterface msg = api.getMessage(result);
            if (msg.getMessageType() == MeiEbdsDevice.MessageType.ERROR) {
                MeiEbdsAcceptorMsgError e = (MeiEbdsAcceptorMsgError) msg;
                return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, e.getError());
            }
            return processAcceptorMessage(msg);
        } catch (TimeoutException ex) {
            Logger.debug("Timeout waiting for device, retry");
            //pool the machine.
            if (retries++ > 100) {
                return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, "Timeout reading from port");
            }
            if (mustCount) {
                hostPollMsg.enableAllDenominations();
            } else {
                hostPollMsg.disableAllDenominations();
            }
            String err = sendMessage(hostPollMsg);
            if (err != null) {
                return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
            }
        }
        return this;
    }

    private DeviceStateInterface processAcceptorMessage(MeiEbdsAcceptorMsgInterface msg) {
        Logger.debug("Received msg : %s == %s", msg.getMessageType().name(), msg.toString());
        switch (msg.getMessageType()) {
            case HostToAcceptor:
            default:
                return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR,
                        String.format("got unexpected message type %s", msg.getMessageType().name()));

            case ENQ: // poll
                String err = sendMessage(hostPollMsg);
                if (err != null) {
                    return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
                }
                break;
            case AcceptorToHost:
                if (currMsg != hostPollMsg) {
                    Logger.error("recived an unexpecteed message %s", msg);
                    break;
                }
                if (result.getAck() == currMsg.getAck()) {
                    Logger.debug("GOT AN ACK FOR HOSTPOOL, flipping ack");
                    hostPollMsg.incAck();
                    currMsg = null;
                    if (mustStore) {
                        mustStore = false;
                        mustReject = false;
                        hostPollMsg.setStackNote();
                        sendMessage(hostPollMsg);
                        hostPollMsg.clearStackNote();
                    } else if (mustReject) {
                        mustStore = false;
                        mustReject = false;
                        hostPollMsg.setReturnNote();
                        sendMessage(hostPollMsg);
                        hostPollMsg.clearReturnNote();
                    }

                }

                break;
        }
        return null;
    }

    private MeiEbdsHostMsg currMsg = null;

    private String sendMessage(MeiEbdsHostMsg msg) {
        if (currMsg != null) {
            Logger.error(String.format("LOST A message without ack : %s, new message %s", currMsg, msg));
        }
        currMsg = msg;
        return api.sendMessage(currMsg);
    }

}
