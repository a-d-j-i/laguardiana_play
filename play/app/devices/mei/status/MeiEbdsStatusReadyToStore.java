package devices.mei.status;

import devices.device.status.DeviceStatusInterface;

/**
 *
 * @author adji
 */
public class MeiEbdsStatusReadyToStore implements DeviceStatusInterface {

    final String slot;

    public MeiEbdsStatusReadyToStore(String slot) {
        this.slot = slot;
    }

    public String getSlot() {
        return slot;
    }

    @Override
    public String toString() {
        return "MeiEbdsStatusReadyToStore{" + "slot=" + slot + '}';
    }

    public boolean is(Enum type) {
        return false;
    }

    public boolean is(Class type) {
        return this.getClass().equals(type);
    }

}
