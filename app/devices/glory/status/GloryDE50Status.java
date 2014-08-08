package devices.glory.status;

import devices.device.status.DeviceStatusInterface;

/**
 *
 * @author adji
 */
public class GloryDE50Status {

    static public enum GloryDE50StatusType implements DeviceStatusInterface {

        OPEN_PORT,
        NEUTRAL,
        PUT_THE_BILLS_ON_THE_HOPER,
        PUT_THE_ENVELOPE_IN_THE_ESCROW,
        COUNTING,
        ESCROW_FULL,
        REMOVE_THE_BILLS_FROM_ESCROW,
        REMOVE_REJECTED_BILLS,
        REMOVE_THE_BILLS_FROM_HOPER,

        READY_TO_STORE,
        STORING,
        
        CANCELING,
        CANCELED,
        REJECTING,
        RETURNED,
        JAM;

        public boolean is(Enum type) {
            return (type == this);
        }

        public boolean is(Class type) {
            return false;
        }
    };

}
