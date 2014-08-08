package devices.glory.status;

import devices.device.status.DeviceStatusInterface;

/**
 *
 * @author adji
 */
public class GloryDE50StatusMachineErrorCode implements DeviceStatusInterface {

    private final int errorCode;

    public GloryDE50StatusMachineErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "GloryDE50StatusMachineErrorCode{" + "errorCode=" + errorCode + '}';
    }

    public boolean is(Enum type) {
        return false;
    }

    public boolean is(Class type) {
        return this.getClass().equals(type);
    }

}
