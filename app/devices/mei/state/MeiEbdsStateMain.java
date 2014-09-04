package devices.mei.state;

import devices.device.DeviceResponseInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskCount;
import devices.device.task.DeviceTaskOpenPort;
import devices.device.task.DeviceTaskReset;
import devices.device.task.DeviceTaskStore;
import devices.device.task.DeviceTaskWithdraw;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.mei.response.MeiEbdsAcceptorMsgError;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatusStored;
import devices.mei.task.MeiEbdsTaskCount;
import devices.device.task.DeviceTaskMessage;
import devices.device.task.DeviceTaskReadTimeout;
import devices.mei.MeiEbdsDevice;
import devices.mei.response.MeiEbdsAcceptorMsgAck.ResponseType;
import static devices.mei.response.MeiEbdsAcceptorMsgAck.ResponseType.*;
import devices.mei.response.MeiEbdsAcceptorMsgEnq;
import devices.mei.status.MeiEbdsStatusReadyToStore;
import play.data.validation.Error;

/**
 *
 * @author adji
 */
public class MeiEbdsStateMain extends MeiEbdsStateAbstract {

    protected void debug(String message, Object... args) {
        //    Logger.debug(message, args);
    }

    public MeiEbdsStateMain(MeiEbdsDevice mei) {
        super(mei);
    }

    private boolean skipEscrowed = false;
    private String lastNoteValue = null;
    private boolean mustCancel = false;
    private int retries = 300;

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract t) {
        boolean ret;
        debug("%s ----------------> Received a task call : %s", mei.toString(), t.toString());
        if (t instanceof DeviceTaskReadTimeout) {
            retries--;
            debug("RETRIES : %d", retries);
            if (retries <= 0) {
                return new MeiEbdsError(mei, "Timeout reading from serial port");
            }
            ret = true;
        } else if (t instanceof DeviceTaskMessage) {
            if (retries <= 300) {
                retries = 300;
            }
            DeviceTaskMessage msgTask = (DeviceTaskMessage) t;
            DeviceResponseInterface response = msgTask.getResponse();
            if (response != null) { // retry
                String err = null;
                if (response instanceof MeiEbdsAcceptorMsgEnq) {
                } else if (response instanceof MeiEbdsAcceptorMsgAck) {
                    MeiEbdsAcceptorMsgAck r = (MeiEbdsAcceptorMsgAck) response;
                    switch ((ResponseType) r.getType()) {
                        case Error:
                            MeiEbdsAcceptorMsgError e = (MeiEbdsAcceptorMsgError) response;
                            err = e.getError();
                            break;
                        case HostToAcceptor:
                        default:
                            err = String.format("%s got unexpected message type %s", mei.toString(), r.getType().name());
                            break;
                        case ENQ: // poll
                            err = mei.sendPollMessage();
                            break;
                        case Extended:
                        case AcceptorToHost:
                            MeiEbdsAcceptorMsgAck lastResponse = (MeiEbdsAcceptorMsgAck) response;
                            if (!mei.isMessageOk(response)) {
                                err = "Error the only valid message is lastResult";
                            } else {
                                if (mei.isAckOk(lastResponse)) { // if ack is not ok, just ignore
                                    t.setReturnValue(true);
                                    return processAcceptorToHostMsg(lastResponse);
                                }
                            }
                    }
                } else {
                    err = "Invalid response type";
                }
                if (err != null) {
                    return new MeiEbdsError(mei, err);
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
            mustCancel = true;
            String err = mei.cancelCount();
            if (err != null) {
                t.setReturnValue(false);
                return new MeiEbdsError(mei, err);
            }
            t.setReturnValue(true);
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
            return new MeiEbdsError(mei, err);
        }
        return this;
    }

// send the first message
    @Override
    public DeviceStateInterface init() {
        String err = mei.sendPollMessage();
        if (err != null) {
            return new MeiEbdsError(mei, err);
        }
        return null;
    }

    @Override
    public String toString() {
        return "MeiEbdsStateMain";
    }

    protected DeviceStateInterface processAcceptorToHostMsg(MeiEbdsAcceptorMsgAck response) {
        if (response.isJammed() || response.isFailure()) {
            mei.notifyListeners(MeiEbdsStatus.JAM);
            retries = 1000;
            return null;
        } else {
            retries = 300;
            if (response.isPowerUp()) {
                mei.notifyListeners(MeiEbdsStatus.NEUTRAL);
            }
        }
        String err = null;
        if (response.isEscrowed()) {
            debug("%s Is escrowed", mei.toString());
            lastNoteValue = response.getNoteSlot();
            if (mustCancel) {
                if (!mei.reject()) {
                    err = "Mei Error in reject";
                } else {
                    err = mei.sendPollMessage();
                }
            } else {
                // skip the first escrowed after store.
                if (!skipEscrowed && lastNoteValue != null) {
                    mei.notifyListeners(new MeiEbdsStatusReadyToStore(lastNoteValue));
                }
            }
        } else if (response.isReturned()) {
            debug("%s Is returned", mei.toString());
            mei.notifyListeners(MeiEbdsStatus.RETURNED);
            lastNoteValue = null;
            err = mei.sendPollMessage();
        } else if (response.isStacked()) {
            debug("%s Is stacked", mei.toString());
            if (lastNoteValue != null) {
                mei.notifyListeners(new MeiEbdsStatusStored(lastNoteValue));
            }
            lastNoteValue = null;
            err = mei.sendPollMessage();
        } else {
            debug("%s Is None", mei.toString());
            if (response.isIdling()) {
                if (mei.isSomeDenominationEnabled()) {
                    mei.notifyListeners(MeiEbdsStatus.COUNTING);
                } else {
                    if (mustCancel) {
                        mei.notifyListeners(MeiEbdsStatus.CANCELED);
                        mustCancel = false;
                    } else {
                        mei.notifyListeners(MeiEbdsStatus.NEUTRAL);
                    }
                }
                lastNoteValue = null;
            }
        }
        skipEscrowed = false;
        if (err != null) {
            return new MeiEbdsError(mei, err);
        }
        return null;
    }
}
