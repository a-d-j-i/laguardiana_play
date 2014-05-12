/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.device.state;

import devices.device.DeviceAbstract;
import devices.device.task.DeviceTaskInterface;
import java.util.concurrent.TimeUnit;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class DeviceStateOperation extends DeviceStateAbstract {

    public DeviceStateOperation(DeviceAbstract.DeviceStateApi api) {
        super(api);
    }

    @Override
    public DeviceStateInterface step() {
        try {
            DeviceTaskInterface deviceTask = getApi().getOperationQueue().poll(1000, TimeUnit.MILLISECONDS);
            if (deviceTask != null) {
                return deviceTask.execute(this);
            }
        } catch (InterruptedException ex) {
            Logger.debug("DeviceWaitForOperation exception : %s", ex.toString());
        }
        return this;
    }
}
