package devices.ioboard.status;

import devices.device.status.*;
import models.Configuration;

/**
 *
 * @author adji
 */
public class IoBoardStatusError extends DeviceStatusError implements DeviceStatusInterface {

    public IoBoardStatusError(String error) {
        super(error);
    }

    @Override
    public String toString() {
        return "IoBoardStatusError{" + "error=" + error + '}';
    }

    public boolean is(Class type) {
        if (DeviceStatusError.class == type) {
            return true;
        }
        return this.getClass().equals(type);
    }

    public boolean canIgnore() {
        return Configuration.isIgnoreIoBoard();
    }

}
