package devices.mei.status;

import devices.device.status.DeviceStatusInterface;

/**
 *
 * @author adji
 */
public enum MeiEbdsStatus implements DeviceStatusInterface {

    CANCELED,
    CANCELING,
    STORING,
    REJECTING,
    READY_TO_STORE,
    RETURNED,
    COUNTING,
    NEUTRAL,
    JAM,;

    public boolean is(Enum type) {
        return (type == this);
    }

    public boolean is(Class type) {
        return false;
    }
}
