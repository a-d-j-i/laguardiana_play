package machines.states;

import machines.status.MachineStatus;
import machines.status.MachineStatusError;

/**
 *
 * @author adji
 */
public class MachineStateStoringError extends MachineStateAbstract {

    private final String error;
    private final Integer currentUserId;

    public MachineStateStoringError(MachineStateContextInterface machine, Integer currentUserId, String error, Object... args) {
        this.error = String.format(error, args);
        this.currentUserId = currentUserId;
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatusError(currentUserId, "ErrorController.onStoringError", "ERROR", error);
    }

}
