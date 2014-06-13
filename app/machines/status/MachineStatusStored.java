package machines.status;

/**
 *
 * @author adji
 */
public class MachineStatusStored extends MachineStatus {

    final String slot;

    public MachineStatusStored(String slot) {
        super(MachineStatusType.STORED);
        this.slot = slot;
    }

    public String getSlot() {
        return slot;
    }

    @Override
    public String toString() {
        return "MachineStatusStored{" + "slot=" + slot + '}';
    }

}
