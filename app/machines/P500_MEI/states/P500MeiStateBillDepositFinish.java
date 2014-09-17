package machines.P500_MEI.states;

import machines.states.MachineStateAbstract;
import machines.status.MachineBillDepositStatus;
import models.BillDeposit;
import models.BillQuantity;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateBillDepositFinish extends MachineStateAbstract {

    private final BillDeposit.FinishCause finishCause;
    private final P500MEIStateContext context;

    public P500MeiStateBillDepositFinish(P500MEIStateContext context, BillDeposit.FinishCause finishCause) {
        this.context = context;
        this.finishCause = finishCause;
    }

    @Override
    public boolean onStart() {
        context.closeBatch();
        BillDeposit billDeposit = BillDeposit.findById(context.getDepositId());
        if (billDeposit != null) {
            Logger.info("Trying to print deposit: %d", billDeposit.depositId);
            billDeposit.closeDeposit(finishCause);
            if (billDeposit.getTotal() > 0) {
                Logger.info("Printing deposit: %d", billDeposit.depositId);
                billDeposit.print(false);
            } else {
                Logger.info("Skipping deposit: %d", billDeposit.depositId);
            }
        } else {
            Logger.info("Deposit: %d not found", billDeposit.depositId);
        }
        return true;
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        BillDeposit billDeposit = BillDeposit.findById(context.getDepositId());
        Long currentSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(context.getDepositId(), BillQuantity.getBillQuantities(billDeposit.currency, billDeposit.getCurrentQuantity(), null),
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
