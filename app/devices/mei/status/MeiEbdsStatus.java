package devices.mei.status;

import devices.device.DeviceStatusInterface;

/**
 *
 * @author adji
 */
public class MeiEbdsStatus implements DeviceStatusInterface {

    public static enum MeiEbdsStatusType {

        ERROR,
        OPEN_PORT,
        CANCELING, CANCELED,
        READY_TO_STORE,
        REJECTING, RETURNED,
        STORING, STORED, NEUTRAL, COUNTING,

    };
    final MeiEbdsStatusType type;

    public MeiEbdsStatus(MeiEbdsStatusType type) {
        this.type = type;
    }

    public MeiEbdsStatusType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "MeiEbdsStatus{" + "type=" + type + '}';
    }

}
