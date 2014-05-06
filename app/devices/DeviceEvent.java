package devices;

import java.util.EventObject;

/**
 *
 * @author adji
 */
public class DeviceEvent extends EventObject {

    final private DeviceStatus state;

    public DeviceEvent(Object source, DeviceStatus state) {
        super(source);
        this.state = state;
    }

    public DeviceStatus getState() {
        return state;
    }
}
