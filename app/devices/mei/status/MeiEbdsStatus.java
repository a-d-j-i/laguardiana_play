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

    public boolean dontLog() {
        return this == NEUTRAL || this == COUNTING;
    }
}
