package devices.mei.response;

import devices.device.DeviceResponseInterface;
import devices.mei.response.MeiEbdsAcceptorMsgAck.ResponseType;

/**
 *
 * @author adji
 */
public class MeiEbdsAcceptorMsgEnq implements DeviceResponseInterface {

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
