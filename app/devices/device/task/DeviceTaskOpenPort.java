package devices.device.task;

/**
 *
 * @author adji
 */
public class DeviceTaskOpenPort extends DeviceTaskAbstract {

    private final String port;

    public DeviceTaskOpenPort(Enum type, String port) {
        super(type);
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
