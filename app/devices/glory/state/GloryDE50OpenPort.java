package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.glory.state.poll.GloryDE50GotoNeutral;
import devices.device.task.DeviceTaskOpenPort;
import devices.glory.GloryDE50Device.GloryDE50DeviceStateApi;
import static devices.glory.GloryDE50Device.GloryDE50TaskType.TASK_OPEN_PORT;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50OpenPort extends GloryDE50StateOperation {

    final String port;

    public GloryDE50OpenPort(GloryDE50DeviceStateApi api, String port) {
        super(api);
        this.port = port;
    }

    @Override
    public GloryDE50StateAbstract step() {
        // wait for operations
        super.step();
        if (api.open(port)) {
            Logger.debug("Port open success");
            return new GloryDE50GotoNeutral(api);
        }
        Logger.debug("Port not open, polling");
        return this;
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
            if ( task.getType() == TASK_OPEN_PORT ) {
                DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
                task.setReturnValue(true);
                return new GloryDE50OpenPort(api, open.getPort());
            }
            return null;
    }

}
