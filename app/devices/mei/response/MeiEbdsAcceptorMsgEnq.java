package devices.mei.response;

import devices.device.DeviceMessageInterface;
import devices.mei.task.MeiEbdsTaskMessage.ResponseType;

/**
 *
 * @author adji
 */
public class MeiEbdsAcceptorMsgEnq implements DeviceMessageInterface {

    public MeiEbdsAcceptorMsgEnq() {
    }

    public ResponseType getType() {
        return ResponseType.ENQ;
    }

    @Override
    public String toString() {
        return "MeiEbdsAcceptorMsgEnq";
    }

}
