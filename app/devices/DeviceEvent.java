package devices;

import java.util.EventObject;
import machines.Machine;

/**
 *
 * @author adji
 */
public class DeviceEvent extends EventObject {

    final private DeviceStatus state;

    public DeviceEvent(DeviceAbstract source, DeviceStatus state) {
        super(source);
        this.state = state;
    }

    public DeviceStatus getState() {
        return state;
    }

    @Override
    public DeviceAbstract getSource() {
        return (DeviceAbstract) source;
    }

    public Machine.DeviceDescription getSourceDevice() {
        return getSource().deviceDescription;
    }
}
