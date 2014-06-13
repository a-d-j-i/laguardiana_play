package machines.status;

import devices.device.status.DeviceStatusInterface;

/**
 *
 * @author adji
 */
public class MachineStatus implements DeviceStatusInterface {

    public static enum MachineStatusType {

        ERROR,
        INITIALIZING,
        NEUTRAL,
        WAITING_FOR_ENVELOPE,
        COUNTING,
        READY_TO_STORE,
        STORING,
        STORED,
        CANCELING,
        CANCELED,
        REJECTING,
        RETURNED,
        JAM,
        // ioboard
        BAG_REMOVED,
        BAG_INPLACE,
        // printer
        PRINTER_NOT_READY,
        PRINTER_READY
    };
    final MachineStatusType type;

    public MachineStatus(MachineStatusType type) {
        this.type = type;
    }

    public MachineStatusType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "MachineStatus " + "type = " + type;
    }

}
