package devices.device.task;

/**
 *
 * @author adji
 */
public class DeviceTaskOpenPort extends DeviceTaskAbstract {

    private final String port;

    public DeviceTaskOpenPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "DeviceTaskOpenPort{" + "port=" + port + '}';
    }

}
