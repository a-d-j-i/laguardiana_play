package devices.ioboard.response;

import devices.device.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public class IoboardErrorResponse implements DeviceResponseInterface {

    private final String error;

    public IoboardErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "IoboardErrorResponse{" + "error=" + error + '}';
    }

}
