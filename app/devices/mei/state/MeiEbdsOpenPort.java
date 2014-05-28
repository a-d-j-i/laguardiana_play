/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.state;

import devices.device.DeviceStatusInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.MeiEbdsDevice.MeiEbdsDeviceStateApi;
import static devices.mei.MeiEbdsDevice.MeiEbdsTaskType.TASK_OPEN_PORT;
import devices.mei.status.MeiEbdsStatusOpenPort;
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
        if (api.open(port)) {
            Logger.debug("Port open success");
            return new MeiEbdsStateMain(api);
        }
        Logger.debug("Port not open, polling");
        return super.step(10000);
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        Logger.debug("MeiEbdsOpenPort got task %s", task.toString());
        if (task.getType() == TASK_OPEN_PORT) {
            DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
            task.setReturnValue(true);
            Logger.debug("MeiEbdsOpenPort new port %s", open.getPort());
            //this.port = open.getPort();
            return new MeiEbdsOpenPort(api, open.getPort());
        }
        return null;
    }

    public DeviceStatusInterface getStatus() {
        return new MeiEbdsStatusOpenPort(port);
    }

    @Override
    public String toString() {
        return "MeiEbdsOpenPort{" + "port=" + port + '}';
    }

}
