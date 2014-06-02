package devices.device.state;

import devices.device.task.DeviceTaskAbstract;

/**
 *
 * @author adji
 */
abstract public class DeviceStateAbstract implements DeviceStateInterface {

    public DeviceStateInterface call(DeviceTaskAbstract task) {
        task.setReturnValue(false);
        return null;
    }

    public DeviceStateInterface init() {
        return this;
    }

    @Override
    public String toString() {
        return "DeviceStateAbstract";
    }

}
