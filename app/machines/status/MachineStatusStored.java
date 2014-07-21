package machines.status;

/**
 *
 * @author adji
 */
public class MachineStatusStored {

    final String slot;

    public MachineStatusStored(String slot) {
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
