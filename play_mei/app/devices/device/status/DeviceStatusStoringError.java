package devices.device.status;

/**
 *
 * @author adji
 */
public class DeviceStatusStoringError extends DeviceStatusError {

    public DeviceStatusStoringError(String error) {
        super(error);
    }

    @Override
    public String toString() {
        return "DeviceStatusStoringError{" + "error=" + error + '}';
    }

    public boolean is(Enum type) {
        return false;
    }

    public boolean is(Class type) {
        return this.getClass().equals(type);
    }

}
