package machines.status;

import java.util.Collection;
import models.BillDeposit;
import models.BillQuantity;

/**
 *
 * @author adji
 */
public class MachineBillDepositStatus extends MachineCountStatus {

    final BillDeposit deposit;

    public MachineBillDepositStatus(BillDeposit deposit, Collection<BillQuantity> billQuantities, Integer currentUserId, String neededAction, String stateName, String message, Long currentSum, Long totalSum) {
        super(currentSum, totalSum, billQuantities, currentUserId, neededAction, stateName, message);
        this.deposit = deposit;
    }

    public BillDeposit getCurrentDeposit() {
        return deposit;
    }

    @Override
    public String toString() {
        return "MachineBillDepositStatus{" + "deposit=" + deposit + '}' + super.toString();
    }

}
