package devices.mei.task;

import devices.device.task.DeviceTaskAbstract;
import java.util.List;

/**
 *
 * @author adji
 */
public class MeiEbdsTaskCount extends DeviceTaskAbstract {

    final private List<Integer> slotList;

    public MeiEbdsTaskCount(Enum type, List<Integer> slotList) {
        super(type);
        this.slotList = slotList;
    }

    public List<Integer> getSlotList() {
        return slotList;
    }

}
