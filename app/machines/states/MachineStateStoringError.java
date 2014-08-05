package machines.states;

import machines.status.MachineStatus;
import machines.status.MachineStatusError;

/**
 *
 * @author adji
 */
public class MachineStateStoringError extends MachineStateAbstract {

    private final String error;

    public MachineStateStoringError(MachineStateApiInterface machine, String error, Object... args) {
        super(machine);
        this.error = String.format(error, args);
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatusError(null, "controller", "onStoringError", "ERROR", error);
    }

}
