package devices.mei.response;

import devices.device.DeviceResponseInterface;
import devices.mei.response.MeiEbdsAcceptorMsgAck.ResponseType;

/**
 *
 * @author adji
 */
public class MeiEbdsAcceptorMsgTimeout implements DeviceResponseInterface {

    public MeiEbdsAcceptorMsgTimeout() {
    }

    public ResponseType getType() {
        return ResponseType.ENQ;
    }

    @Override
    public String toString() {
        return "MeiEbdsAcceptorMsgTimeout";
    }

}
