/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskOpenPort;
import devices.glory.state.GloryDE50OpenPort;
import devices.glory.state.poll.GloryDE50GotoNeutral;
import devices.mei.MeiEbdsDeviceStateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsOpenPort extends MeiEbdsStateOperation {

    final String port;

    public MeiEbdsOpenPort(MeiEbdsDeviceStateApi api, String port) {
        super(api);
        this.port = port;
    }

    @Override
    public DeviceStateInterface step() {
        super.step(3000);
        if (api.open(port)) {
            Logger.debug("Port open success");
            return new MeiEbdsStateMain(api);
        }
        Logger.debug("Port not open, polling");
        return this;
    }

    public DeviceStateInterface call(DeviceTaskOpenPort task) {
        return new MeiEbdsOpenPort(api, task.getPort());
    }
}
