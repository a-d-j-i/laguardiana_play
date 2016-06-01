package devices.device.status;

import java.security.InvalidParameterException;

/**
 *
 * @author adji
 */
public class DeviceStatusError implements DeviceStatusInterface {

    final protected String error;

    public DeviceStatusError(String error) {
        if (error == null) {
            throw new InvalidParameterException("Error must be not null!!!");
        }
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "DeviceStatusError{" + "error=" + error + '}';
    }

    public boolean is(Enum type) {
        return false;
    }

    public boolean is(Class type) {
        return this.getClass().equals(type);
    }

    public boolean dontLog() {
        return false;
    }

}
