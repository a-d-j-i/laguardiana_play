package machines.P500_MEI.states;

import machines.states.*;
import machines.status.MachineStatus;
import machines.status.MachineStatusError;
import models.BillDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateError extends MachineStateAbstract {

    private final P500MEIStateContext context;
    private final MachineStateAbstract prevState;
    private final String error;

    public P500MeiStateError(MachineStateAbstract prevState, P500MEIStateContext context, String error, Object... args) {
        this.prevState = prevState;
        this.context = context;
        this.error = String.format(error, args);
        Logger.error("-----------------> MACHINE ERROR : %s", error);
    }

    @Override
    public boolean onReset() {
        debug("Reset on device %s error %s", context.toString(), error);
        boolean ret = context.reset();
        if (ret) {
            context.setCurrentState(prevState);
        }
        return ret;
    }

    @Override
    public boolean onCancelDepositEvent() {
        context.closeBatch();
        return context.setCurrentState(new P500MeiStateBillDepositFinish(context, BillDeposit.FinishCause.FINISH_CAUSE_ERROR));
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatusError(context.getCurrentUserId(), "ErrorController.onError", "ERROR", error);
    }

    @Override
    public String toString() {
        return "P500MeiStateError{" + "context=" + context + ", prevState=" + prevState + ", error=" + error + '}';
    }

}
