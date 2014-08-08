package machines.states;

import machines.status.MachineStatus;
import machines.status.MachineStatusError;

/**
 *
 * @author adji
 */
public class MachineStateError extends MachineStateAbstract {

    private final String error;
    private final Integer currentUserId;

    public MachineStateError(MachineStateApiInterface machine, Integer currentUserId, String error, Object... args) {
        super(machine);
        this.error = String.format(error, args);
        this.currentUserId = currentUserId;
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatusError(currentUserId, "ErrorController.onError", "ERROR", error);
    }

}
