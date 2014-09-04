package devices.ioboard.response;

import devices.device.DeviceResponseInterface;
import java.util.HashMap;

/**
 *
 * @author adji
 */
public class IoboardStateResponse implements DeviceResponseInterface {

    public enum SHUTTER_STATE {

        SHUTTER_START_CLOSE(0),
        SHUTTER_START_OPEN(1),
        SHUTTER_RUN_OPEN(2),
        SHUTTER_RUN_CLOSE(3),
        SHUTTER_RUN_OPEN_PORT(4),
        SHUTTER_RUN_CLOSE_PORT(5),
        SHUTTER_STOP_OPEN(6),
        SHUTTER_STOP_CLOSE(7),
        SHUTTER_OPEN(8),
        SHUTTER_CLOSED(9);
        static final HashMap< Byte, SHUTTER_STATE> reverse = new HashMap< Byte, SHUTTER_STATE>();

        static {
            for (SHUTTER_STATE s : SHUTTER_STATE.values()) {
                reverse.put(s.stId, s);
            }
        }
        private Byte stId;

        private SHUTTER_STATE(int stId) {
            this.stId = (byte) stId;
        }

        public static SHUTTER_STATE factory(int stId) {
            return reverse.get((byte) stId);
        }

        public boolean isOpen() {
            return this == SHUTTER_OPEN;
        }
    };

    public enum BAG_STATE {

        BAG_STATE_ERROR(0),
        BAG_STATE_REMOVED(1),
        BAG_STATE_INPLACE(2),
        BAG_STATE_TAKING_START(3),
        BAG_STATE_TAKING_STEP1(4),
        BAG_STATE_TAKING_STEP2(5),
        BAG_STATE_PUTTING_START(6),
        BAG_STATE_PUTTING_1(7),
        BAG_STATE_PUTTING_2(8),
        BAG_STATE_PUTTING_3(9),
        BAG_STATE_PUTTING_4(10),
        BAG_STATE_PUTTING_5(11),
        BAG_STATE_PUTTING_6(12),;
        static final HashMap< Byte, BAG_STATE> reverse = new HashMap< Byte, BAG_STATE>();

        static {
            for (BAG_STATE s : BAG_STATE.values()) {
                reverse.put(s.stId, s);
            }
        }
        private Byte stId;

        private BAG_STATE(int stId) {
            this.stId = (byte) stId;
        }

        public static BAG_STATE factory(int stId) {
            return reverse.get((byte) stId);
        }
    }

    private final SHUTTER_STATE shutterState;
    private final Boolean bagAproveState;
    private final BAG_STATE bagState;
    private final Integer lockState;

    public IoboardStateResponse(Integer bagSt, Integer shutterSt, Integer lockSt, Boolean bagAproved) {
        //Logger.debug("IOBOARD setSTATE : bagSt %s, setShutterState : %s, setLockState : %d, bagAproved : %s", bagSt, shutterSt, lockSt, bagAproved);
        bagState = BAG_STATE.factory(bagSt);
        shutterState = SHUTTER_STATE.factory(shutterSt);
        lockState = lockSt;
        // The bag changed the state.
        bagAproveState = bagAproved;
    }

    public SHUTTER_STATE getShutterState() {
        return shutterState;
    }

    public Boolean isBagAproveState() {
        return bagAproveState;
    }

    public BAG_STATE getBagState() {
        return bagState;
    }

    public Integer getLockState() {
        return lockState;
    }

    @Override
    public String toString() {
        return "IoboardStateResponse{" + "shutterState=" + shutterState + ", bagAproveState=" + bagAproveState + ", bagState=" + bagState + ", lockState=" + lockState + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.shutterState != null ? this.shutterState.hashCode() : 0);
        hash = 19 * hash + (this.bagAproveState != null ? this.bagAproveState.hashCode() : 0);
        hash = 19 * hash + (this.bagState != null ? this.bagState.hashCode() : 0);
        hash = 19 * hash + (this.lockState != null ? this.lockState.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IoboardStateResponse other = (IoboardStateResponse) obj;
        if (this.shutterState != other.shutterState) {
            return false;
        }
        if (this.bagAproveState != other.bagAproveState && (this.bagAproveState == null || !this.bagAproveState.equals(other.bagAproveState))) {
            return false;
        }
        if (this.bagState != other.bagState) {
            return false;
        }
        if (this.lockState != other.lockState && (this.lockState == null || !this.lockState.equals(other.lockState))) {
            return false;
        }
        return true;
    }

}
