package devices.ioboard.response;

import devices.device.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public class IoboardCriticalResponse implements DeviceResponseInterface {

    private final String error;

    public IoboardCriticalResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "IoboardCriticalResponse{" + "error=" + error + '}';
    }

}
