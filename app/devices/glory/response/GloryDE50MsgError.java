package devices.glory.response;

import devices.device.DeviceResponseInterface;
import devices.mei.response.MeiEbdsAcceptorMsgAck.ResponseType;

/**
 *
 * @author adji
 */
public class GloryDE50MsgError implements DeviceResponseInterface {

    private final String error;

    public GloryDE50MsgError(String error) {
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
