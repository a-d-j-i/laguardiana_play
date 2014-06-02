package devices.mei.state;

import devices.device.DeviceMessageInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceMessageTask;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.MeiEbds;
import devices.mei.MeiEbdsDevice;
import devices.mei.MeiEbdsDevice.MeiEbdsTaskType;
import static devices.mei.MeiEbdsDevice.MeiEbdsTaskType.TASK_CANCEL;
import static devices.mei.MeiEbdsDevice.MeiEbdsTaskType.TASK_REJECT;
import static devices.mei.MeiEbdsDevice.MessageType.AcceptorToHost;
import static devices.mei.MeiEbdsDevice.MessageType.ENQ;
import static devices.mei.MeiEbdsDevice.MessageType.HostToAcceptor;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.mei.response.MeiEbdsAcceptorMsgError;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatus.MeiEbdsStatusType;
import devices.mei.status.MeiEbdsStatusStored;
import devices.mei.task.MeiEbdsTaskCount;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsStateMain extends MeiEbdsStateAbstract {

    public MeiEbdsStateMain(MeiEbds mei) {
        super(mei);
    }

    private boolean skipEscrowed = false;
    private boolean mustCount = false;
    private String lastNoteValue = "";

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        boolean ret;
        Logger.debug("----------------> Received a task call : %s", task.getType().name());
        switch ((MeiEbdsTaskType) task.getType()) {
            case TASK_MESSAGE:
                DeviceMessageTask msgTask = (DeviceMessageTask) task;
                DeviceMessageInterface msg = msgTask.getMessage();
                if (msg != null) { // retry
                    String err = null;
                    switch ((MeiEbdsDevice.MessageType) msg.getType()) {
                        case Error:
                            MeiEbdsAcceptorMsgError e = (MeiEbdsAcceptorMsgError) msg;
                            err = e.getError();
                            break;
                        case HostToAcceptor:
                        default:
                            err = String.format("got unexpected message type %s", msg.getType().name());
                            break;
                        case ENQ: // poll
                            err = mei.sendPollMessage();
                            break;
                        case Extended: // TODO: check if a different treatment is needed
                        case AcceptorToHost:
                            MeiEbdsAcceptorMsgAck lastMessage = (MeiEbdsAcceptorMsgAck) msg;
                            if (!mei.isMessageOk(msg)) {
                                err = "Error the only valid message is lastResult";
                            } else {
                                if (mei.isAckOk(lastMessage)) { // if ack is not ok, just ignore
                                    return processAcceptorToHostMsg(lastMessage);
                                }
                            }
                    }
                    if (err != null) {
                        return new MeiEbdsError(mei, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
                    }
                }
                // TODO: Check
                ret = true;
                break;

            case TASK_OPEN_PORT:
                DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
                if (mei.open(open.getPort())) {
                    Logger.debug("MeiEbdsStateMain new port %s", open.getPort());
                    task.setReturnValue(true);
                    return this;
                } else {
                    Logger.debug("MeiEbdsStateMain new port %s failed to open", open.getPort());
                    task.setReturnValue(false);
                    return new MeiEbdsOpenPort(mei);
                }
            case TASK_RESET: // todo: implement it.
                task.setReturnValue(true);
                return null;
            case TASK_CANCEL:
                task.setReturnValue(true);
                return new MeiEbdsStateCancel(mei);
            default:
                Logger.debug("ignoring task %s %s", task.getType().name(), task.toString());
                task.setReturnValue(false);
                return null;
            case TASK_COUNT:
                MeiEbdsTaskCount cnt = (MeiEbdsTaskCount) task;
                ret = mei.count(cnt.getSlotList());
                if (ret) {
                    mustCount = true;
                }
                break;
            case TASK_STORE:
                skipEscrowed = true;
                ret = mei.store();
                if (ret) {
                    mei.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.STORING));
                }
                break;
            case TASK_REJECT:
                skipEscrowed = true;
                ret = mei.reject();
                if (ret) {
                    mei.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.REJECTING));
                }
                break;
        }
        task.setReturnValue(ret);
        String err = mei.sendPollMessage();
        if (err != null) {
            return new MeiEbdsError(mei, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
        }
        return null;
    }

// send the first message
    @Override
    public DeviceStateInterface init() {
        String err = mei.sendPollMessage();
        if (err != null) {
            return new MeiEbdsError(mei, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
        }
        return null;
    }

    @Override
    public String toString() {
        return "MeiEbdsStateMain";
    }

    protected DeviceStateInterface processAcceptorToHostMsg(MeiEbdsAcceptorMsgAck lastMsg) {
        if (lastMsg.isJammed() || lastMsg.isFailure()) {
            mei.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.JAM));
            return null;
        } else {
            if (lastMsg.isPowerUp()) {
                mei.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.NEUTRAL));
            }
        }
        String err = null;
        if (lastMsg.isEscrowed()) {
            Logger.debug("Is escrowed");
            // skip the first escrowed after store.
            if (!skipEscrowed) {
                mei.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.READY_TO_STORE));
            }
            lastNoteValue = lastMsg.getNoteSlot();
        } else if (lastMsg.isReturned()) {
            Logger.debug("Is returned");
            mei.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.RETURNED));
            lastNoteValue = "";
            err = mei.sendPollMessage();
        } else if (lastMsg.isStacked()) {
            Logger.debug("Is stacked");
            if (!lastNoteValue.isEmpty()) {
                mei.notifyListeners(new MeiEbdsStatusStored(lastNoteValue));
            }
            lastNoteValue = "";
            err = mei.sendPollMessage();
        } else {
            Logger.debug("Is None");
            if (lastMsg.isIdling()) {
                if (mustCount) {
                    mei.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.COUNTING));
                } else {
                    mei.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.NEUTRAL));
                }
            }
            lastNoteValue = "";
        }
        mustCount = false;
        skipEscrowed = false;
        if (err != null) {
            return new MeiEbdsError(mei, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
        }
        return null;
    }
}
