package devices.mei.state;

import devices.device.DeviceMessageInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskCount;
import devices.device.task.DeviceTaskOpenPort;
import devices.device.task.DeviceTaskReset;
import devices.device.task.DeviceTaskStore;
import devices.device.task.DeviceTaskWithdraw;
import devices.mei.MeiEbds;
import devices.mei.operation.MeiEbdsHostMsg;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.mei.response.MeiEbdsAcceptorMsgError;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatusStored;
import devices.mei.task.MeiEbdsTaskCount;
import devices.mei.task.MeiEbdsTaskMessage;
import static devices.mei.task.MeiEbdsTaskMessage.ResponseType.AcceptorToHost;
import static devices.mei.task.MeiEbdsTaskMessage.ResponseType.ENQ;
import static devices.mei.task.MeiEbdsTaskMessage.ResponseType.Extended;
import static devices.mei.task.MeiEbdsTaskMessage.ResponseType.HostToAcceptor;

/**
 *
 * @author adji
 */
public class MeiEbdsStateMain extends MeiEbdsStateAbstract {

    protected void debug(String message, Object... args) {
        //Logger.debug(message, args);
    }

    public MeiEbdsStateMain(MeiEbds mei) {
        super(mei);
    }

    private boolean skipEscrowed = false;
    private String lastNoteValue = "";
    private boolean mustCancel = false;

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract t) {
        boolean ret;
        debug("%s ----------------> Received a task call : %s", mei.toString(), t.toString());
        if (t instanceof MeiEbdsTaskMessage) {
            MeiEbdsTaskMessage msgTask = (MeiEbdsTaskMessage) t;
            DeviceMessageInterface response = msgTask.getResponse();
            if (response != null) { // retry
                String err = null;
                switch ((MeiEbdsTaskMessage.ResponseType) response.getType()) {
                    case Error:
                        MeiEbdsAcceptorMsgError e = (MeiEbdsAcceptorMsgError) response;
                        err = e.getError();
                        break;
                    case HostToAcceptor:
                    default:
                        err = String.format("%s got unexpected message type %s", mei.toString(), response.getType().name());
                        break;
                    case ENQ: // poll
                        err = mei.sendPollMessage();
                        break;
                    case Extended: // TODO: check if a different treatment is needed
                    case AcceptorToHost:
                        MeiEbdsAcceptorMsgAck lastResponse = (MeiEbdsAcceptorMsgAck) response;
                        MeiEbdsHostMsg message = msgTask.getMessage();
                        if (!mei.isMessageOk(response)) {
                            err = "Error the only valid message is lastResult";
                        } else {
                            if (mei.isAckOk(lastResponse)) { // if ack is not ok, just ignore
                                return processAcceptorToHostMsg(message, lastResponse);
                            }
                        }
                }
                if (err != null) {
                    return new MeiEbdsError(mei, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
                }
            }
            // TODO: Check
            ret = true;
        } else if (t instanceof DeviceTaskOpenPort) {
            DeviceTaskOpenPort open = (DeviceTaskOpenPort) t;
            if (mei.open(open.getPort())) {
                debug("%s MeiEbdsStateMain new port %s", mei.toString(), open.getPort());
                t.setReturnValue(true);
                return this;
            } else {
                debug("%s MeiEbdsStateMain new port %s failed to open", mei.toString(), open.getPort());
                t.setReturnValue(false);
                return new MeiEbdsOpenPort(mei);
            }
        } else if (t instanceof DeviceTaskReset) {
            t.setReturnValue(true);
            return null;
        } else if (t instanceof DeviceTaskCancel) {
            t.setReturnValue(true);
            mustCancel = true;
            String err = mei.cancelCount();
            if (err != null) {
                return new MeiEbdsError(mei, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
            }
            mei.notifyListeners(MeiEbdsStatus.CANCELING);
            return null;
        } else if (t instanceof DeviceTaskStore) {
            skipEscrowed = true;
            ret = mei.store();
            if (ret) {
                mei.notifyListeners(MeiEbdsStatus.STORING);
            }
        } else if (t instanceof DeviceTaskWithdraw) {
            skipEscrowed = true;
            ret = mei.reject();
            if (ret) {
                mei.notifyListeners(MeiEbdsStatus.REJECTING);
            }
        } else if (t instanceof DeviceTaskCount) {
            if (t instanceof MeiEbdsTaskCount) {
                MeiEbdsTaskCount cnt = (MeiEbdsTaskCount) t;
                ret = mei.count(cnt.getDesiredQuantity());
            } else {
                ret = mei.count(null);
            }
        } else {
            debug("%s ignoring task %s", mei.toString(), t.toString());
            t.setReturnValue(false);
            return null;
        }
        t.setReturnValue(ret);
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

    protected DeviceStateInterface processAcceptorToHostMsg(MeiEbdsHostMsg message, MeiEbdsAcceptorMsgAck response) {
        if (response.isJammed() || response.isFailure()) {
            mei.notifyListeners(MeiEbdsStatus.JAM);
            return null;
        } else {
            if (response.isPowerUp()) {
                mei.notifyListeners(MeiEbdsStatus.NEUTRAL);
            }
        }
        String err = null;
        if (response.isEscrowed()) {
            debug("%s Is escrowed", mei.toString());
            if (mustCancel) {
                if (!mei.reject()) {
                    err = "Mei Error in reject";
                } else {
                    err = mei.sendPollMessage();
                }
            } else {
                // skip the first escrowed after store.
                if (!skipEscrowed) {
                    mei.notifyListeners(MeiEbdsStatus.READY_TO_STORE);
                }
            }
            lastNoteValue = response.getNoteSlot();
        } else if (response.isReturned()) {
            debug("%s Is returned", mei.toString());
            mei.notifyListeners(MeiEbdsStatus.RETURNED);
            lastNoteValue = "";
            err = mei.sendPollMessage();
        } else if (response.isStacked()) {
            debug("%s Is stacked", mei.toString());
            if (!lastNoteValue.isEmpty()) {
                mei.notifyListeners(new MeiEbdsStatusStored(lastNoteValue));
            }
            lastNoteValue = "";
            err = mei.sendPollMessage();
        } else {
            debug("%s Is None", mei.toString());
            if (response.isIdling()) {
                if (message.isSomeDenominationEnabled()) {
                    mei.notifyListeners(MeiEbdsStatus.COUNTING);
                } else {
                    if (mustCancel) {
                        mei.notifyListeners(MeiEbdsStatus.CANCELED);
                        mustCancel = false;
                    } else {
                        mei.notifyListeners(MeiEbdsStatus.NEUTRAL);
                    }
                }
                lastNoteValue = "";
            }
        }
        skipEscrowed = false;
        if (err != null) {
            return new MeiEbdsError(mei, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
        }
        return null;
    }
}
