package devices.glory.status;

import devices.device.status.DeviceStatusInterface;

/**
 *
 * @author adji
 */
public class GloryDE50Status {

    static public enum GloryDE50StatusType implements DeviceStatusInterface {

        ERROR,
        OPEN_PORT,
        NEUTRAL,
        READY_TO_STORE,
        STORING,
        STORED,
        PUT_THE_BILLS_ON_THE_HOPER,
        COUNTING,
        ESCROW_FULL,
        PUT_THE_ENVELOPE_IN_THE_ESCROW,
        INITIALIZING,
        REMOVE_THE_BILLS_FROM_ESCROW,
        REMOVE_REJECTED_BILLS,
        REMOVE_THE_BILLS_FROM_HOPER,
        CANCELING,
        BAG_COLLECTED,
        JAM;

        public boolean is(Enum type) {
            return (type == this);
        }

        public boolean is(Class type) {
            return false;
        }
    };

}
