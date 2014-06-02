package devices.mei.response;

import devices.device.DeviceMessageInterface;
import devices.mei.MeiEbdsDevice.MessageType;

/**
 *
 * @author adji
 */
public class MeiEbdsAcceptorMsgEnq implements DeviceMessageInterface {

    public MeiEbdsAcceptorMsgEnq() {
    }

    public MessageType getType() {
        return MessageType.ENQ;
    }

    @Override
    public String toString() {
        return "MeiEbdsAcceptorMsgEnq";
    }

}
