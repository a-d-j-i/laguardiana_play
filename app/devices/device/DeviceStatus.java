package devices.device;

/**
 *
 * @author adji
 */
public class DeviceStatus {

    static public enum STATUS {

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
        JAM,
        ERROR;

    };
    public String error;

    public DeviceStatus(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "error=" + error;
    }

}
