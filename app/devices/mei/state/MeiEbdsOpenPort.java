/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import static devices.mei.MeiEbdsDevice.MeiEbdsTaskType.TASK_OPEN_PORT;
import devices.mei.MeiEbdsDeviceStateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsOpenPort extends MeiEbdsStateAbstract {

    final String port;

    public MeiEbdsOpenPort(MeiEbdsDeviceStateApi api, String port) {
        super(api);
        this.port = port;
    }

    @Override
    public DeviceStateInterface step() {
        super.step();
        if (api.open(port)) {
            Logger.debug("Port open success");
            return new MeiEbdsStateMain(api);
        }
        Logger.debug("Port not open, polling");
        return this;
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task.getType() == TASK_OPEN_PORT) {
            DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
            task.setReturnValue(true);
            return new MeiEbdsOpenPort(api, open.getPort());
        }
        return null;
    }
}
