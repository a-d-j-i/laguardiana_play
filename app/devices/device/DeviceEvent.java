package devices.device;

import java.util.EventObject;

/**
 *
 * @author adji
 */
public class DeviceEvent extends EventObject {

    final private DeviceStatusInterface status;

    public DeviceEvent(DeviceAbstract source, DeviceStatusInterface status) {
        super(source);
        this.status = status;
    }

    public DeviceStatusInterface getStatus() {
        return status;
    }

    @Override
    public DeviceAbstract getSource() {
        return (DeviceAbstract) source;
    }

    public Enum getSourceDevice() {
        return getSource().machineDeviceId;
    }

    @Override
    public String toString() {
        return "DeviceEvent{" + "status=" + status + '}';
    }

}
