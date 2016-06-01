package devices.device;

import devices.device.status.DeviceStatusInterface;
import java.util.EventObject;

/**
 *
 * @author adji
 */
public class DeviceEvent extends EventObject {

    final private DeviceStatusInterface status;

    public DeviceEvent(DeviceInterface source, DeviceStatusInterface status) {
        super(source);
        this.status = status;
    }

    public DeviceStatusInterface getStatus() {
        return status;
    }

    @Override
    public DeviceInterface getSource() {
        return (DeviceInterface) source;
    }

    @Override
    public String toString() {
        return "DeviceEvent{ source = " + getSource() + " status=" + status + '}';
    }

}