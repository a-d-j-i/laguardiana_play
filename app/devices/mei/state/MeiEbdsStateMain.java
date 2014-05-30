package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.MeiEbdsDevice.MeiEbdsDeviceStateApi;
import devices.mei.MeiEbdsDevice.MeiEbdsTaskType;
import static devices.mei.MeiEbdsDevice.MeiEbdsTaskType.TASK_CANCEL;
import static devices.mei.MeiEbdsDevice.MeiEbdsTaskType.TASK_REJECT;
import static devices.mei.MeiEbdsDevice.MessageType.AcceptorToHost;
import static devices.mei.MeiEbdsDevice.MessageType.ENQ;
import static devices.mei.MeiEbdsDevice.MessageType.HostToAcceptor;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.mei.response.MeiEbdsAcceptorMsgError;
import devices.mei.response.MeiEbdsAcceptorMsgInterface;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatus.MeiEbdsStatusType;
import devices.mei.status.MeiEbdsStatusStored;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsStateMain extends MeiEbdsStateAbstract {

    public MeiEbdsStateMain(MeiEbdsDeviceStateApi api) {
        super(api);
    }

    private boolean skipEscrowed = false;
    private boolean mustCount = false;
    private int lastNoteValue = 0;

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        boolean ret;
        Logger.debug("----------------> Received a task call : %s", task.getType().name());
        switch ((MeiEbdsTaskType) task.getType()) {
            case TASK_OPEN_PORT:
                DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
                task.setReturnValue(true);
                return new MeiEbdsOpenPort(api, open.getPort());
            case TASK_RESET: // todo: implement it.
                task.setReturnValue(true);
                return null;
            case TASK_CANCEL:
                task.setReturnValue(true);
                return new MeiEbdsStateCancel(api);
            default:
                Logger.debug("ignoring task %s %s", task.getType().name(), task.toString());
                task.setReturnValue(false);
                return null;
            case TASK_COUNT:
                ret = api.getMei().count();
                if (ret) {
                    mustCount = true;
                }
                break;
            case TASK_STORE:
                skipEscrowed = true;
                ret = api.getMei().store();
                if (ret) {
                    api.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.STORING));
                }
                break;
            case TASK_REJECT:
                skipEscrowed = true;
                ret = api.getMei().reject();
                if (ret) {
                    api.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.REJECTING));
                }
                break;
        }
        task.setReturnValue(ret);
        String err = api.getMei().sendPollMessage();
        if (err != null) {
            return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
        }
        return null;
    }

// send the first message
    @Override
    public DeviceStateInterface init() {
        String err = api.getMei().sendPollMessage();
        if (err != null) {
            return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
        }
        return null;
    }

    @Override
    public DeviceStateInterface step() {
        DeviceTaskAbstract deviceTask = api.poll();
        if (deviceTask != null) {
            Logger.debug("Got task : %s, executing", deviceTask);
            return deviceTask.execute(this);
        }
        try {
            String err = null;
            MeiEbdsAcceptorMsgInterface msg = api.getMei().getMessage();
            if (msg != null) { // retry
                switch (msg.getMessageType()) {
                    case Error:
                        MeiEbdsAcceptorMsgError e = (MeiEbdsAcceptorMsgError) msg;
                        err = e.getError();
                        break;
                    case HostToAcceptor:
                    default:
                        err = String.format("got unexpected message type %s", msg.getMessageType().name());
                        break;
                    case ENQ: // poll
                        err = api.getMei().sendPollMessage();
                        break;
                    case AcceptorToHost:
                        MeiEbdsAcceptorMsgAck lastMsg = (MeiEbdsAcceptorMsgAck) msg;
                        err = api.getMei().checkAck(lastMsg);
                        if (err == null) {
                            if (lastMsg.isEscrowed()) {
                                Logger.debug("Is escrowed");
                                // skip the first escrowed after store.
                                if (!skipEscrowed) {
                                    api.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.READY_TO_STORE));
                                }
                                lastNoteValue = lastMsg.getNoteValue();
                            } else if (lastMsg.isReturned()) {
                                Logger.debug("Is returned");
                                api.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.RETURNED));
                                lastNoteValue = 0;
                                err = api.getMei().sendPollMessage();
                            } else if (lastMsg.isStacked()) {
                                Logger.debug("Is stacked");
                                api.notifyListeners(new MeiEbdsStatusStored(lastNoteValue));
                                lastNoteValue = 0;
                                err = api.getMei().sendPollMessage();
                            } else {
                                Logger.debug("Is None");
                                if (lastMsg.isIdling()) {
                                    if (mustCount) {
                                        api.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.COUNTING));
                                    } else {
                                        api.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.NEUTRAL));
                                    }
                                }
                                lastNoteValue = 0;
                            }
                        }
                        break;
                }
                mustCount = false;
                skipEscrowed = false;
                if (err != null) {
                    return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
                }
            }
        } catch (InterruptedException ex) { // got a task.
        }
        return null;
    }

    @Override
    public String toString() {
        return "MeiEbdsStateMain";
    }

}
