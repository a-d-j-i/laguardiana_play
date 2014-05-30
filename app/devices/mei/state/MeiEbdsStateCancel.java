package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.mei.MeiEbdsDevice.MeiEbdsDeviceStateApi;
import static devices.mei.MeiEbdsDevice.MessageType.AcceptorToHost;
import static devices.mei.MeiEbdsDevice.MessageType.ENQ;
import static devices.mei.MeiEbdsDevice.MessageType.HostToAcceptor;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.mei.response.MeiEbdsAcceptorMsgError;
import devices.mei.response.MeiEbdsAcceptorMsgInterface;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatus.MeiEbdsStatusType;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsStateCancel extends MeiEbdsStateAbstract {

    public MeiEbdsStateCancel(MeiEbdsDeviceStateApi api) {
        super(api);
    }

    // send the first message
    @Override
    public DeviceStateInterface init() {
        String err = api.getMei().cancelCount();
        if (err != null) {
            return new MeiEbdsError(api, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
        }
        api.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.CANCELING));
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
                                if (!api.getMei().reject()) {
                                    err = "Mei Error in reject";
                                } else {
                                    err = api.getMei().sendPollMessage();
                                }
                            } else if (lastMsg.isReturned()) {
                                Logger.debug("Is returned");
                                err = api.getMei().sendPollMessage();
                            } else if (lastMsg.isStacked()) {
                                Logger.debug("Is stacked");
                                err = "Error stacked during cancel";
                            } else {
                                if (lastMsg.isIdling()) {
                                    api.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.CANCELED));
                                    return new MeiEbdsStateMain(api);
                                }
                                Logger.debug("Is None");
                            }
                        }
                        break;
                }
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
        return "MeiEbdsStateCancel";
    }
}
