package devices.device.status;

import devices.device.DeviceStatusInterface;

/**
 *
 * @author adji
 */
abstract public class DeviceStatusError implements DeviceStatusInterface {

    final String error;

    public DeviceStatusError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "DeviceStatusError{" + "error=" + error + '}';
    }

}
