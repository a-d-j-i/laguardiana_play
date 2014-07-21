package devices.mei.task;

import devices.device.task.DeviceTaskCount;
import java.util.Map;

/**
 *
 * @author adji
 */
public class MeiEbdsTaskCount extends DeviceTaskCount {

    final private Map<String, Integer> desiredQuantity;

    public MeiEbdsTaskCount(Map<String, Integer> desiredQuantity) {
        super();
        this.desiredQuantity = desiredQuantity;
    }

    public Map<String, Integer> getDesiredQuantity() {
        return desiredQuantity;
    }

    @Override
    public String toString() {
        return "DeviceTaskCount{" + "desiredQuantity=" + desiredQuantity + '}';
    }

}
