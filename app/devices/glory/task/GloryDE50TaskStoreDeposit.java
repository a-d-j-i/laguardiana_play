/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.task;

import devices.device.state.DeviceStateAbstract;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.state.poll.Store;

/**
 *
 * @author adji
 */
public class GloryDE50TaskStoreDeposit extends GloryDE50TaskAbstract<Boolean> {

    protected DeviceStateAbstract call(GloryDE50StateAbstract currentState) {
        boolean ret = currentState.acceptStore();
        setReturnValue(ret);
        if (ret) {
            return new Store(currentState.getApi());
        }
        return null;
    }

}
