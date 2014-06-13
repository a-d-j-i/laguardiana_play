package devices.glory.task;

import devices.device.task.DeviceTaskAbstract;
import java.util.Map;

/**
 *
 * @author adji
 */
public class GloryDE50TaskCount extends DeviceTaskAbstract {

    final private Map<String, Integer> desiredQuantity;
    final private Integer currency;

    public GloryDE50TaskCount(Enum type, Map<String, Integer> desiredQuantity, Integer currency) {
        super(type);
        this.desiredQuantity = desiredQuantity;
        this.currency = currency;
    }

    public Integer getCurrency() {
        return currency;
    }

    public Map<String, Integer> getDesiredQuantity() {
        return desiredQuantity;
    }

}
