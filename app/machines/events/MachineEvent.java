package machines.events;

import java.util.EventObject;
import machines.Machine;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class MachineEvent extends EventObject {

    private final MachineStatus status;

    public MachineEvent(Machine source, MachineStatus status) {
        super(source);
        this.status = status;
    }

    @Override
    public Machine getSource() {
        return (Machine) source;
    }

    public MachineStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "MachineEvent : source = " + source.toString() + " status = " + status.toString();
    }

}
