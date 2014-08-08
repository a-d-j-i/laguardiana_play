package machines.P500_MEI.states;

import machines.states.*;
import machines.status.MachineStatus;
import machines.status.MachineStatusError;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateError extends MachineStateAbstract {

    private final String error;
    private final Integer currentUserId;

    public P500MeiStateError(MachineStateAbstract prevState, Integer currentUserId, String error, Object... args) {
        this.error = String.format(error, args);
        this.currentUserId = currentUserId;
        Logger.error("-----------------> MACHINE ERROR : %s", error);
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatusError(currentUserId, "ErrorController.onError", "ERROR", error);
    }

}
