/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskInterface;
import devices.mei.MeiEbdsDeviceStateApi;
import java.util.concurrent.TimeUnit;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsStateOperation implements DeviceStateInterface {

    protected final MeiEbdsDeviceStateApi api;

    public MeiEbdsStateOperation(MeiEbdsDeviceStateApi api) {
        this.api = api;
    }

    public DeviceStateInterface init() {
        return this;
    }

    boolean isError() {
        return false;
    }

    public boolean cancelDeposit() {
        return false;
    }

    @Override
    public DeviceStateInterface step() {
        return step(1000);
    }

    public DeviceStateInterface step(int timeoutMS) {
        try {
            DeviceTaskInterface deviceTask = api.poll(timeoutMS, TimeUnit.MILLISECONDS);
            if (deviceTask != null) {
                return deviceTask.execute(this);
            }
        } catch (InterruptedException ex) {
            Logger.debug("DeviceWaitForOperation exception : %s", ex.toString());
        }
        return this;
    }

    public DeviceStateInterface call(DeviceTaskInterface task) {
        return null;
    }

}
