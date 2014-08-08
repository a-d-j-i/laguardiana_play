package machines.states;

import machines.status.MachineStatus;
import machines.status.MachineStatusError;
import play.Logger;

/**
 *
 * @author adji
 */
public class MachineStateError extends MachineStateAbstract {

    private final String error;
    private final Integer currentUserId;

    public MachineStateError(MachineStateAbstract prevState, Integer currentUserId, String error, Object... args) {
        this.error = String.format(error, args);
        this.currentUserId = currentUserId;
        Logger.error("-----------------> MACHINE ERROR : %s", error);
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatusError(currentUserId, "ErrorController.onError", "ERROR", error);
    }

}
