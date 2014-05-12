package devices.device.task;

import devices.device.state.DeviceStateInterface;

/**
 *
 * @author adji
 * @param <T>
 */
public interface DeviceTaskInterface<T> {

    // Executed by the outher thread.
    public T get();

    // Executed by the inner thread.
    public DeviceStateInterface execute(DeviceStateInterface currentState);

    public void setReturnValue(T returnValue);
}
