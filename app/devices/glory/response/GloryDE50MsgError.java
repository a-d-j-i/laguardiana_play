package devices.glory.response;

import devices.device.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public class GloryDE50MsgError implements DeviceResponseInterface {

    private final String error;

    public GloryDE50MsgError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "MeiEbdsAcceptorMsgError " + "error=" + error;
    }

}
