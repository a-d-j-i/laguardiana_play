package devices.device.status;

import devices.device.DeviceStatusInterface;

/**
 *
 * @author adji
 */
abstract public class DeviceStatusOpenPort implements DeviceStatusInterface {

    final String port;

    public DeviceStatusOpenPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "DeviceStatusOpenPort{" + "port=" + port + '}';
    }

}
