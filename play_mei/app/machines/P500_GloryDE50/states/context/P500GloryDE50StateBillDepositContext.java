package machines.P500_GloryDE50.states.context;

import devices.glory.status.GloryDE50StatusCurrentCount;
import devices.glory.task.GloryDE50TaskCount;
import java.util.HashMap;
import java.util.Map;
import models.BillDeposit;
import models.db.LgBillType;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateBillDepositContext extends P500GloryDE50StateContext {

    final Integer depositId;
    final Integer currentUserId;
    public Integer batchId = null;
    Map<LgBillType, Integer> currentQuantity = new HashMap<LgBillType, Integer>();
    Long currentSum = 0L;

    public P500GloryDE50StateBillDepositContext(P500GloryDE50StateContext context, BillDeposit d) {
        super(context.machine, context.glory);
        this.depositId = d.depositId;
        this.currentUserId = d.user.userId;

    }

    public boolean count() {
        BillDeposit billDeposit = BillDeposit.findById(depositId);
        Integer c = 1;
        if (billDeposit.currency != null) {
            c = billDeposit.currency.numericId;
        }
        Logger.debug("Calling count on device %s, currency : %d", glory.toString(), c);
        return glory.submitSynchronous(new GloryDE50TaskCount(c, new HashMap<String, Integer>()));
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public String toString() {
        return "P500GloryDE50StateBillDepositContext{" + "depositId=" + depositId + ", currentUserId=" + currentUserId + ", batchId=" + batchId + ", currentQuantity=" + currentQuantity + ", currentSum=" + currentSum + '}';
    }

    public void setCurrentQuantity(GloryDE50StatusCurrentCount st) {
        GloryDE50StatusCurrentCount currentCountStatus = (GloryDE50StatusCurrentCount) st;
        currentQuantity = glory.getQuantities(currentCountStatus.getCurrentQuantity());
        currentSum = 0L;
        for (Map.Entry<LgBillType, Integer> e : currentQuantity.entrySet()) {
            currentSum += (e.getValue() * e.getKey().denomination);
        }
        Logger.debug("Setting current count from : %s to %s", st.toString(), toString());
    }

    public Integer getDepositId() {
        return depositId;
    }

    public Map<LgBillType, Integer> getCurrentQuantity() {
        return currentQuantity;
    }

    public Long getCurrentSum() {
        return currentSum;
    }

}
