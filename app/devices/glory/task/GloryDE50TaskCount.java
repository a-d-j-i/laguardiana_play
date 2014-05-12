/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.task;

import devices.device.state.DeviceStateAbstract;
import devices.glory.state.poll.Count;
import devices.glory.state.GloryDE50StateAbstract;
import java.util.Map;

/**
 *
 * @author adji
 */
public class GloryDE50TaskCount extends GloryDE50TaskAbstract<Boolean> {

    final Map<Integer, Integer> desiredQuantity;
    final Integer currency;

    public GloryDE50TaskCount(final Map<Integer, Integer> desiredQuantity, final Integer currency) {
        this.desiredQuantity = desiredQuantity;
        this.currency = currency;
    }

    protected DeviceStateAbstract call(GloryDE50StateAbstract currentState) {
        boolean ret = currentState.acceptCount();
        setReturnValue(ret);
        if (ret) {
            return new Count(currentState.getApi(), desiredQuantity, currency);
        }
        return null;
    }

}
