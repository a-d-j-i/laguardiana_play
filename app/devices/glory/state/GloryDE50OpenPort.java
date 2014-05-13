package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.glory.GloryDE50DeviceStateApi;
import devices.glory.state.poll.GloryDE50GotoNeutral;
import devices.device.task.DeviceTaskOpenPort;
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

    public DeviceStateInterface call(DeviceTaskOpenPort task) {
        return new GloryDE50OpenPort(api, task.getPort());
    }

}
