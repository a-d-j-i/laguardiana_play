package devices.device;

import java.util.EventListener;

/**
 *
 * @author adji
 */
public interface DeviceEventListener extends EventListener {

    void onDeviceEvent(DeviceEvent evt);
}
