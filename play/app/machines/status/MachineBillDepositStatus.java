package machines.status;

import java.util.Collection;
import models.BillDeposit;
import models.BillQuantity;

/**
 *
 * @author adji
 */
public class MachineBillDepositStatus extends MachineCountStatus {

    final Integer depositId;

    public MachineBillDepositStatus(Integer depositId, Collection<BillQuantity> billQuantities, Integer currentUserId,
            String neededAction, String stateName, Long currentSum, Long totalSum) {
        super(currentSum, totalSum, billQuantities, currentUserId, neededAction, stateName);
        this.depositId = depositId;
    }

    public BillDeposit getCurrentDeposit() {
        return BillDeposit.findById(depositId);
    }

    @Override
    public String toString() {
        return "MachineBillDepositStatus{" + "depositId=" + depositId + '}' + super.toString();
    }

}
