package devices.glory.task;

import devices.device.task.DeviceTaskAbstract;
import java.util.Map;

/**
 *
 * @author adji
 */
public class GloryDE50TaskCount extends DeviceTaskAbstract {

    final Map<Integer, Integer> desiredQuantity;
    final Integer currency;

    public GloryDE50TaskCount(final Map<Integer, Integer> desiredQuantity, final Integer currency) {
        this.desiredQuantity = desiredQuantity;
        this.currency = currency;
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        return desiredQuantity;
    }

    public Integer getCurrency() {
        return currency;
    }

}
