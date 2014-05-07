package devices;

/**
 *
 * @author adji
 */
public class DeviceStatus {

    public String error;

    public DeviceStatus(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "error=" + error;
    }

}
