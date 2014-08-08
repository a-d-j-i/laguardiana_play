package machines.P500_MEI.states;

import machines.status.MachineBillDepositStatus;
import models.BillDeposit;
import models.BillQuantity;
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
        if (!context.count()) {
            Logger.error("Can't start MachineActionBillDeposit error in api.count");
            return false;
        }
        return true;
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        BillDeposit billDeposit = context.getBillDeposit();
        if (billDeposit.getTotal() > 0) {
            context.setCurrentState(new P500MeiStateBillDepositContinue(context));
        }
        return super.getStatus();
    }

    @Override
    public boolean onAcceptDepositEvent() {
        return false;
    }

    @Override
    public boolean onCancelDepositEvent() {
        context.closeBatch();
        BillDeposit billDeposit = context.getBillDeposit();
        if (billDeposit.getTotal() > 0) {
            return context.setCurrentState(new P500MeiStateAccepting(context));
        } else {
            return context.setCurrentState(new P500MeiStateCanceling(context));
        }
    }

    @Override
    public String toString() {
        return "P500MeiStateBillDepositStart{" + super.toString() + '}';
    }

}
