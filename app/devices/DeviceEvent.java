package devices;

import java.util.EventObject;

/**
 *
 * @author adji
 */
public class DeviceEvent extends EventObject {

    private DeviceState state;

    public DeviceEvent(Object source, DeviceState state) {
        super(source);
        this.state = state;
    }

    public DeviceState getState() {
        return state;
    }
}
