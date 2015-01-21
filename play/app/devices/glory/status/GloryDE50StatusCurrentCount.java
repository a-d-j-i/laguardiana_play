package devices.glory.status;

import devices.device.status.DeviceStatusCountIntreface;
import devices.device.status.DeviceStatusInterface;
import java.util.Map;

/**
 *
 * @author adji
 */
public class GloryDE50StatusCurrentCount implements DeviceStatusInterface, DeviceStatusCountIntreface {

    final private Map<String, Integer> currentQuantity;
    final private Map<String, Integer> desiredQuantity;

    public GloryDE50StatusCurrentCount(Map<String, Integer> currentQuantity, Map<String, Integer> desiredQuantity) {
        this.currentQuantity = currentQuantity;
        this.desiredQuantity = desiredQuantity;
    }

    public Map<String, Integer> getCurrentQuantity() {
        return currentQuantity;
    }

    public Map<String, Integer> getDesiredQuantity() {
        return desiredQuantity;
    }

    @Override
    public String toString() {
        return "GloryDE50StatusCurrentCount{" + "currentQuantity=" + currentQuantity + ", desiredQuantity=" + desiredQuantity + '}';
    }

    public boolean is(Enum type) {
        return false;
    }

    public boolean is(Class type) {
        return this.getClass().equals(type);
    }

}
