package machines.states;

import machines.MachineAbstract;
import machines.status.MachineStatus;
import machines.status.MachineStatusError;

/**
 *
 * @author adji
 */
public class MachineStateError extends MachineStateAbstract {

    private final String error;

    public MachineStateError(MachineStateApiInterface machine, String error, Object... args) {
        super(machine);
        this.error = String.format(error, args);
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatusError(null, "controller", "onError", "ERROR", error, error);
    }

}
