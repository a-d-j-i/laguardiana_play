package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.glory.GloryDE50Device;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50StateOpenPort extends GloryDE50StateAbstract {

    public GloryDE50StateOpenPort(GloryDE50Device api) {
        super(api);
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        Logger.debug("GloryDE50OpenPort got task %s", task.toString());
        if (task instanceof DeviceTaskOpenPort) {
            DeviceTaskOpenPort openPort = (DeviceTaskOpenPort) task;
            if (api.open(openPort.getPort())) {
                Logger.debug("GloryDE50OpenPort new port %s", openPort.getPort());
                task.setReturnValue(true);
                return new GloryDE50StateWaitForOperation(api);
            }
            Logger.debug("GloryDE50OpenPort new port %s failed to open", openPort.getPort());
            task.setReturnValue(false);
            return this;
        }
        return super.call(task);
    }

    @Override
    public String toString() {
        return "GloryDE50StateOpenPort";
    }

}