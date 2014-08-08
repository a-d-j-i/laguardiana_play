package machines.P500_GloryDE50.states;

import machines.P500_GloryDE50.states.context.P500GloryDE50StateContext;
import machines.states.MachineStateAbstract;
import machines.status.MachineStatus;
import machines.status.MachineStatusError;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateStoringError extends P500GloryDE50StateError {

    public P500GloryDE50StateStoringError(P500GloryDE50StateContext context, MachineStateAbstract prevState, String error, Object... args) {
        super(context, prevState, error, args);
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatusError(null, "ErrorController.onStoringError", "ERROR", error);
    }

    @Override
    public String toString() {
        return "P500GloryDE50StateStoringError{" + "error=" + error + ", context=" + context + '}';
    }

    @Override
    public boolean onStoringErrorReset() {
        return context.storingErrorReset();
    }
}
