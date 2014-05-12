/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.state;

import devices.device.DeviceClassCounterIntreface;
import devices.device.DeviceStatus;
import devices.device.state.DeviceStateCounterAdaptor;
import devices.glory.state.Error;
import devices.glory.state.Error.COUNTER_CLASS_ERROR_CODE;
import play.Logger;

/**
 * TODO: Use play jobs for this.
 *
 * @author adji
 */
abstract public class MeiEbdsStateAbstract extends DeviceStateCounterAdaptor implements DeviceClassCounterIntreface {

    MeiEbdsStateAbstract sendMeiOperation(MeiEbdsOperationInterface cmd) {
        if (cmd != null) {
            MeiEbdsOperationResponse response = api.sendMeiEbdsOperation(cmd, false);
            if (response.isError()) {
                String error = response.getError();
                Logger.error("Error %s sending cmd : %s", error, cmd.getDescription());
                return new Error(api, COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, error);
            }
        }
        return null;
    }

    protected void notifyListeners(DeviceStatus.STATUS status) {
        api.notifyListeners(status);
    }

    public boolean clearError() {
        return false;
    }

    public String getError() {
        return null;
    }
}
