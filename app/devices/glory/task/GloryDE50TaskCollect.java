/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.task;

import devices.device.state.DeviceStateAbstract;
import devices.glory.state.poll.Collect;
import devices.glory.state.GloryDE50StateAbstract;

/**
 *
 * @author adji
 */
public class GloryDE50TaskCollect extends GloryDE50TaskAbstract<Boolean> {

    protected DeviceStateAbstract call(GloryDE50StateAbstract currentState) {
        boolean ret = currentState.acceptCollect();
        setReturnValue(ret);
        if (ret) {
            return new Collect(currentState.getApi());
        }
        return null;
    }

}
