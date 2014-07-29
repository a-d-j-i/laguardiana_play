package machines.P500_MEI.states;

import machines.states.MachineStateApiInterface;
import machines.status.MachineBillDepositStatus;
import models.BillDeposit;
import models.BillQuantity;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateBillDepositStart extends P500MeiStateBillDepositContinue {

    public P500MeiStateBillDepositStart(MachineStateApiInterface machine, Integer userId, Integer billDepositId) {
        super(machine, userId, billDepositId, null);
    }

    @Override
    public boolean onStart() {
        BillDeposit billDeposit = BillDeposit.findById(billDepositId);
        if (!machine.count(billDeposit.currency)) {
            Logger.error("Can't start MachineActionBillDeposit error in api.count");
            return false;
        }
        return true;
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        BillDeposit billDeposit = BillDeposit.findById(billDepositId);
        Long currentSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(billDeposit, BillQuantity.getBillQuantities(billDeposit.currency, billDeposit.getCurrentQuantity(), null),
                currentUserId, "BillDepositController.mainloop", "IDLE", "BillDepositMain, todo", currentSum, currentSum);
    }

    @Override
    public boolean onAcceptDepositEvent() {
        return false;
    }

    @Override
    public boolean onCancelDepositEvent() {
        closeBatch();
        BillDeposit billDeposit = BillDeposit.findById(billDepositId);
        if (billDeposit.getTotal() > 0) {
            return machine.setCurrentState(new P500MeiStateAccepting(machine, currentUserId, billDepositId, batchId));
        } else {
            return machine.setCurrentState(new P500MeiStateCanceling(machine, currentUserId, billDepositId, batchId));
        }
    }

    @Override
    public String toString() {
        return "P500MeiStateBillDepositStart{" + super.toString() + '}';
    }

}
