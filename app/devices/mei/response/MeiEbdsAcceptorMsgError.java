package devices.mei.response;

import devices.device.DeviceMessageInterface;
import devices.mei.MeiEbdsDevice.MessageType;

/**
 *
 * @author adji
 */
public class MeiEbdsAcceptorMsgError implements DeviceMessageInterface {

    private final String error;

    public MeiEbdsAcceptorMsgError(String error) {
        this.error = error;
    }

    public MessageType getType() {
        return MessageType.Error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "MeiEbdsAcceptorMsgError " + "error=" + error;
    }

}
