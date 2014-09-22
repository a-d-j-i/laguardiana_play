package machines.P500_MEI.states;

import machines.status.MachineBillDepositStatus;
import models.BillDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateBillDepositStart extends P500MeiStateBillDepositContinue {

    public P500MeiStateBillDepositStart(P500MEIStateContext context) {
        super(context);
    }

    @Override
    public boolean onStart() {
        debug("sending count");
        if (!context.count()) {
            context.setCurrentState(new P500MeiStateError(this, context, "Can't start MachineActionBillDeposit error in api.count"));
            return false;
        }
        return true;
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        BillDeposit billDeposit = BillDeposit.findById(context.getDepositId());
        if (billDeposit.getTotal() > 0) {
            context.setCurrentState(new P500MeiStateBillDepositContinue(context));
        }
        return super.getStatus("IDLE");
    }

    @Override
    public boolean onAcceptDepositEvent() {
        return false;
    }

    @Override
    public boolean onCancelDepositEvent() {
        context.closeBatch();
        BillDeposit billDeposit = BillDeposit.findById(context.getDepositId());
        if (billDeposit.getTotal() > 0) {
            return context.setCurrentState(new P500MeiStateAccepting(context));
        } else {
            return context.setCurrentState(new P500MeiStateCanceling(context));
        }
    }

    @Override
    public String toString() {
        return "P500MeiStateBillDepositStart{" + context.toString() + '}';
    }

}
