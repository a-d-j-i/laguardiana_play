package devices.mei.response;

import devices.device.DeviceMessageInterface;
import devices.mei.task.MeiEbdsTaskMessage.ResponseType;

/**
 *
 * @author adji
 */
public class MeiEbdsAcceptorMsgError implements DeviceMessageInterface {

    private final String error;

    public MeiEbdsAcceptorMsgError(String error) {
        this.error = error;
    }

    public ResponseType getType() {
        return ResponseType.Error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "MeiEbdsAcceptorMsgError " + "error=" + error;
    }

}
