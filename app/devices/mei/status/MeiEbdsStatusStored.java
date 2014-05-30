package devices.mei.status;

import devices.device.DeviceStatusInterface;

/**
 *
 * @author adji
 */
public class MeiEbdsStatusStored extends MeiEbdsStatus implements DeviceStatusInterface {

    final Integer slot;

    public MeiEbdsStatusStored(Integer slot) {
        super(MeiEbdsStatusType.STORED);
        this.slot = slot;
    }

    @Override
    public String toString() {
        return "MeiEbdsStatusStored{" + "slot=" + slot + '}';
    }
}
