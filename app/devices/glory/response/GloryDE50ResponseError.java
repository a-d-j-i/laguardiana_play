package devices.glory.response;

import devices.device.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public class GloryDE50ResponseError extends GloryDE50Response implements DeviceResponseInterface {

    protected String error;

    public GloryDE50ResponseError(String error) {
        this.error = error;
    }

    @Override
    public boolean isError() {
        return true;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "GloryDE50ResponseError " + "error=" + error;
    }
}
