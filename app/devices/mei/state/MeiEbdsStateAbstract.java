package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.mei.MeiEbdsDevice.MeiEbdsDeviceStateApi;
import java.util.concurrent.TimeUnit;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class MeiEbdsStateAbstract implements DeviceStateInterface {

    protected final MeiEbdsDeviceStateApi api;

    public MeiEbdsStateAbstract(MeiEbdsDeviceStateApi api) {
        this.api = api;
    }

    public DeviceStateInterface call(DeviceTaskAbstract task) {
        task.setReturnValue(false);
        return null;
    }

    public DeviceStateInterface init() {
        return this;
    }

    public DeviceStateInterface step() {
        return step(200);
    }

    public DeviceStateInterface step(int timeout) {
        try {
            DeviceTaskAbstract deviceTask = api.poll(timeout, TimeUnit.MILLISECONDS);
            if (deviceTask != null) {
                DeviceStateInterface ret = deviceTask.execute(this);
                return ret;
            }
        } catch (InterruptedException ex) {
            Logger.debug("MeiEbdsStateAbstract exception : %s", ex.toString());
        }
        return this;
    }

    @Override
    public String toString() {
        return "MeiEbdsStateAbstract";
    }

}
