package devices.ioboard.status;

import devices.device.status.DeviceStatusInterface;
import devices.ioboard.response.IoboardStateResponse;

/**
 *
 * @author adji
 */
public class IoboardStatus implements DeviceStatusInterface {

    public enum IoboardBagApprovedState {

        BAG_APROVED,
        BAG_NOT_APROVED,
        BAG_APROVE_WAIT,
        BAG_APROVE_CONFIRM,;
    }
    private final IoboardStateResponse.SHUTTER_STATE shutterState;
    private final IoboardStateResponse.BAG_STATE bagState;
    private final IoboardBagApprovedState bagApprovedState;
    private final Integer lockState;

    public IoboardStatus(IoboardStateResponse r, IoboardBagApprovedState bagApprovedState) {
        this.bagApprovedState = bagApprovedState;
        this.shutterState = r.getShutterState();
        this.bagState = r.getBagState();
        this.lockState = r.getLockState();
    }

    public IoboardStateResponse.SHUTTER_STATE getShutterState() {
        return shutterState;
    }

    public IoboardStateResponse.BAG_STATE getBagState() {
        return bagState;
    }

    public Integer getLockState() {
        return lockState;
    }

    public IoboardBagApprovedState getBagApprovedState() {
        return bagApprovedState;
    }

    @Override
    public String toString() {
        return "IoboardStatus{" + "shutterState=" + shutterState + ", bagState=" + bagState + ", bagApprovedState=" + bagApprovedState + ", lockState=" + lockState + '}';
    }

    public boolean is(Enum type) {
        return false;
    }

    public boolean is(Class type) {
        return this.getClass().equals(type);
    }

    public boolean dontLog() {
        return true;
    }

}
