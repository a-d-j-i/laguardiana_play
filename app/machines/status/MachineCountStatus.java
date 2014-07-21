package machines.status;

import java.util.Collection;
import models.BillQuantity;

/**
 *
 * @author adji
 */
public class MachineCountStatus extends MachineStatus {

    private final Long currentSum;
    private final Long totalSum;
    final Collection<BillQuantity> billQuantities;

    public MachineCountStatus(Long currentSum, Long totalSum, Collection<BillQuantity> billQuantities, Integer currentUserId, String neededAction, String stateName, String message) {
        super(currentUserId, neededAction, stateName, message);
        this.currentSum = currentSum;
        this.totalSum = totalSum;
        this.billQuantities = billQuantities;
    }

    public Collection<BillQuantity> getBillQuantities() {
        return billQuantities;
    }

    public Long getCurrentSum() {
        return currentSum;
    }

    public Long getTotalSum() {
        return totalSum;
    }

    @Override
    public String toString() {
        return "MachineCountStatus{" + "currentSum=" + currentSum + ", totalSum=" + totalSum + ", billQuantities=" + billQuantities + '}' + super.toString();
    }

}
