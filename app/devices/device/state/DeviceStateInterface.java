package devices.device.state;

import devices.device.task.DeviceTaskAbstract;

/**
 *
 * @author adji
 */
public interface DeviceStateInterface {

    public DeviceStateInterface init();

    public DeviceStateInterface step();

    public DeviceStateInterface call(DeviceTaskAbstract task);

}
