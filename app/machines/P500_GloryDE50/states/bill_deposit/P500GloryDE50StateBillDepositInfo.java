package machines.P500_GloryDE50.states.bill_deposit;

import java.util.HashMap;
import java.util.Map;
import models.db.LgBillType;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateBillDepositInfo {

    public final Integer currentUserId;
    public final Integer billDepositId;
    public Integer batchId = null;
    Map<LgBillType, Integer> currentQuantity = new HashMap<LgBillType, Integer>();
    public Long currentSum = 0L;

    public P500GloryDE50StateBillDepositInfo(Integer currentUserId, Integer billDepositId) {
        this.currentUserId = currentUserId;
        this.billDepositId = billDepositId;
    }

    @Override
    public String toString() {
        return "P500GloryDE50StateBillDepositInfo{" + "currentUserId=" + currentUserId + ", billDepositId=" + billDepositId + ", batchId=" + batchId + ", currentQuantity=" + currentQuantity + ", currentSum=" + currentSum + '}';
    }

}
