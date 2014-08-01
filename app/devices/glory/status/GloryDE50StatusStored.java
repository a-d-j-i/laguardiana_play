package devices.glory.status;

import devices.device.status.DeviceStatusInterface;
import java.util.Map;

/**
 *
 * @author adji
 */
public class GloryDE50StatusStored implements DeviceStatusInterface {

    final private Map<String, Integer> currentQuantity;

    public GloryDE50StatusStored(Map<String, Integer> currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public Map<String, Integer> getCurrentQuantity() {
        return currentQuantity;
    }

    @Override
    public String toString() {
        return "GloryDE50StatusStored{" + "currentQuantity=" + currentQuantity + '}';
    }

    public boolean is(Enum type) {
        return false;
    }

    public boolean is(Class type) {
        return this.getClass().equals(type);
    }

}
