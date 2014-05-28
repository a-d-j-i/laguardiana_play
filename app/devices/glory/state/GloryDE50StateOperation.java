package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.glory.GloryDE50Device.GloryDE50DeviceStateApi;
import java.util.concurrent.TimeUnit;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class GloryDE50StateOperation extends GloryDE50StateAbstract {

    public GloryDE50StateOperation(GloryDE50DeviceStateApi api) {
        super(api);
    }

    @Override
    public DeviceStateInterface step() {
        return step(1000);
    }

    public DeviceStateInterface step(int timeoutMS) {
        try {
            DeviceTaskAbstract deviceTask = api.poll(timeoutMS, TimeUnit.MILLISECONDS);
            if (deviceTask != null) {
                return deviceTask.execute(this);
            }
        } catch (InterruptedException ex) {
            Logger.debug("DeviceWaitForOperation exception : %s", ex.toString());
        }
        return this;
    }
}
