package devices.device;

import devices.device.task.DeviceTaskAbstract;
import java.util.List;
import java.util.concurrent.Future;

/**
 *
 * @author adji
 */
public interface DeviceInterface {

    public void start();

    public void stop();

    public void addEventListener(DeviceEventListener listener);

    public void removeEventListener(DeviceEventListener listener);

    public Future<Boolean> submit(final DeviceTaskAbstract deviceTask);

    public boolean submitSynchronous(final DeviceTaskAbstract deviceTask);

    public boolean setProperty(String property, String value);

    public List<String> getNeededProperties();

    @Override
    public String toString();

}
