package devices.glory.task;

import devices.device.task.DeviceTaskCount;
import java.util.Map;

/**
 *
 * @author adji
 */
public class GloryDE50TaskCount extends DeviceTaskCount {

    final private Map<String, Integer> desiredQuantity;
    private final Integer currency;

    public GloryDE50TaskCount(Integer currency, Map<String, Integer> desiredQuantity) {
        super();
        this.desiredQuantity = desiredQuantity;
        this.currency = currency;
    }

    public Integer getCurrency() {
        return currency;
    }

    public Map<String, Integer> getDesiredQuantity() {
        return desiredQuantity;
    }

    @Override
    public String toString() {
        return "GloryDE50TaskCount{" + "desiredQuantity=" + desiredQuantity + ", currency=" + currency + " " + super.toString() + '}';
    }

}
