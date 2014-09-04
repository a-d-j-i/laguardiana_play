package devices.ioboard.status;

import devices.device.status.*;

/**
 *
 * @author adji
 */
public class IoboardStatusCriticalError implements DeviceStatusInterface {

    final String error;

    public IoboardStatusCriticalError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "IoboardStatusCriticalError{" + "error=" + error + '}';
    }

    public boolean is(Enum type) {
        return false;
    }

    public boolean is(Class type) {
        return this.getClass().equals(type);
    }

}
