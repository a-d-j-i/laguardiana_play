package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.mei.MeiEbds;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatus.MeiEbdsStatusType;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsStateCancel extends MeiEbdsStateMain {

    public MeiEbdsStateCancel(MeiEbds mei) {
        super(mei);
    }

    // send the first message
    @Override
    public DeviceStateInterface init() {
        String err = mei.cancelCount();
        if (err != null) {
            return new MeiEbdsError(mei, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
        }
        mei.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.CANCELING));
        return null;
    }

    @Override
    protected DeviceStateInterface processAcceptorToHostMsg(MeiEbdsAcceptorMsgAck lastMsg) {
        String err = null;
        if (lastMsg.isEscrowed()) {
            Logger.debug("Is escrowed");
            if (!mei.reject()) {
                err = "Mei Error in reject";
            } else {
                err = mei.sendPollMessage();
            }
        } else if (lastMsg.isReturned()) {
            Logger.debug("Is returned");
            err = mei.sendPollMessage();
        } else if (lastMsg.isStacked()) {
            Logger.debug("Is stacked");
            err = "Error stacked during cancel";
        } else {
            if (lastMsg.isIdling()) {
                mei.notifyListeners(new MeiEbdsStatus(MeiEbdsStatusType.CANCELED));
                return new MeiEbdsStateMain(mei);
            }
            Logger.debug("Is None");
        }
        if (err != null) {
            return new MeiEbdsError(mei, MeiEbdsError.COUNTER_CLASS_ERROR_CODE.MEI_EBDS_APPLICATION_ERROR, err);
        }
        return null;
    }

    @Override
    public String toString() {
        return "MeiEbdsStateCancel";
    }
}
