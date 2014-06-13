/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.state;

import devices.device.status.DeviceStatusInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.MeiEbds;
import static devices.mei.MeiEbdsDevice.MeiEbdsTaskType.TASK_OPEN_PORT;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatus.MeiEbdsStatusType;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsOpenPort extends MeiEbdsStateAbstract {

    public MeiEbdsOpenPort(MeiEbds mei) {
        super(mei);
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        Logger.debug("MeiEbdsOpenPort got task %s", task.toString());
        if (task.getType() == TASK_OPEN_PORT) {
            DeviceTaskOpenPort openPort = (DeviceTaskOpenPort) task;
            if (mei.open(openPort.getPort())) {
                Logger.debug("MeiEbdsOpenPort new port %s", openPort.getPort());
                task.setReturnValue(true);
                return new MeiEbdsStateMain(mei);
            } else {
                Logger.debug("MeiEbdsOpenPort new port %s failed to open", openPort.getPort());
                task.setReturnValue(false);
                return this;
            }
        }
        return null;
    }

    public DeviceStatusInterface getStatus() {
        return new MeiEbdsStatus(MeiEbdsStatusType.OPEN_PORT);
    }

    @Override
    public String toString() {
        return "MeiEbdsOpenPort";
    }

}
