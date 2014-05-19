package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.mei.MeiEbdsDeviceStateApi;
import java.util.concurrent.TimeUnit;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsStateAbstract implements DeviceStateInterface {

    protected final MeiEbdsDeviceStateApi api;

    public MeiEbdsStateAbstract(MeiEbdsDeviceStateApi api) {
        this.api = api;
    }

    public DeviceStateInterface call(DeviceTaskAbstract t) {
        return null;
    }

    public DeviceStateInterface init() {
        return this;
    }

    public DeviceStateInterface step() {
        try {
            DeviceTaskAbstract deviceTask = api.poll(200, TimeUnit.MILLISECONDS);
            if (deviceTask != null) {
                return deviceTask.execute(this);
            }
        } catch (InterruptedException ex) {
            Logger.debug("MeiEbdsStateAbstract exception : %s", ex.toString());
        }
        return this;
    }
}
