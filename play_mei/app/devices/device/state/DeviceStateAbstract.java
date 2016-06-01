package devices.device.state;

import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskReadTimeout;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class DeviceStateAbstract implements DeviceStateInterface {

    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskReadTimeout) { // skip
            task.setReturnValue(true);
            return null;
        }
        Logger.error("Unexpected task : %s", task.toString());
        task.setReturnValue(false);
        return null;
    }
    private boolean initialized = false;

    public DeviceStateInterface doInit() {
        initialized = true;
        return init();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public DeviceStateInterface init() {
        return this;
    }

    public void finish() {
    }

    @Override
    public String toString() {
        return "DeviceStateAbstract";
    }

}
