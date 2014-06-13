package devices.device.events;

import devices.device.DeviceEvent;
import java.util.EventListener;

/**
 *
 * @author adji
 */
public interface DeviceEventListener extends EventListener {

    void onDeviceEvent(DeviceEvent evt);
}
