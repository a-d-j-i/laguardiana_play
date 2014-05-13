package devices.device.task;

import devices.device.state.DeviceStateInterface;

/**
 *
 * @author adji
 */
public interface DeviceTaskInterface {

    // Executed by the outher thread.
    public boolean get();

    // Executed by the inner thread.
    public DeviceStateInterface execute(DeviceStateInterface currentState);

    @Override
    public String toString();

}
