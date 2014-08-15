package machines.P500_MEI.states;

import machines.states.MachineStateAbstract;
import machines.status.MachineBillDepositStatus;
import models.BillDeposit;
import models.BillQuantity;
import models.db.LgDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateBillDepositFinish extends MachineStateAbstract {

    private final LgDeposit.FinishCause finishCause;
    private final P500MEIStateContext context;

    public P500MeiStateBillDepositFinish(P500MEIStateContext context, LgDeposit.FinishCause finishCause) {
        this.context = context;
        this.finishCause = finishCause;
    }

    @Override
    public boolean onStart() {
        context.closeDeposit(finishCause);
        return true;
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        BillDeposit billDeposit = context.getBillDeposit();
        Long currentSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(billDeposit, BillQuantity.getBillQuantities(billDeposit.currency, billDeposit.getCurrentQuantity(), null),
                context.getCurrentUserId(), "BillDepositController.finish", "FINISH", currentSum, currentSum);
    }

    @Override
    public boolean onConfirmDepositEvent() {
        return context.setCurrentState(new P500MeiStateWaiting(context));
    }

    @Override
    public String toString() {
        return "P500MeiStateBillDepositFinish{" + "finishCause=" + finishCause + ", context=" + context + '}';
    }

}
