package devices.device.status;

/**
 *
 * @author adji
 */
public class DeviceStatusError implements DeviceStatusInterface {

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

    public boolean is(Enum type) {
        return false;
    }

    public boolean is(Class type) {
        return this.getClass().equals(type);
    }

}
