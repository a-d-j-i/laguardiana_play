package machines.P500_GloryDE50.states.bill_deposit;

import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskWithdraw;
import devices.glory.task.GloryDE50TaskCount;
import java.util.HashMap;
import java.util.Map;
import machines.P500_GloryDE50.states.P500GloryDE50StateContext;
import models.BillDeposit;
import models.db.LgBillType;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateBillDepositContext extends P500GloryDE50StateContext {

    public Integer batchId = null;
    Map<LgBillType, Integer> currentQuantity = new HashMap<LgBillType, Integer>();
    public Long currentSum = 0L;

    public P500GloryDE50StateBillDepositContext(P500GloryDE50StateContext context) {
        super(context.machine, context.glory, context.depositId, context.currentUserId);
    }

    boolean count() {
        BillDeposit billDeposit = BillDeposit.findById(depositId);
        Integer c = 1;
        if (billDeposit.currency != null) {
            c = billDeposit.currency.numericId;
        }
        Logger.debug("Calling count on device %s, currency : %d", glory.toString(), c);
        return glory.submitSynchronous(new GloryDE50TaskCount(c, new HashMap<String, Integer>()));
    }

    public boolean cancel() {
        return glory.submitSynchronous(new DeviceTaskCancel());
    }

    public boolean withdraw() {
        return glory.submitSynchronous(new DeviceTaskWithdraw());
    }

    @Override
    public String toString() {
        return "P500GloryDE50StateBillDepositContext{" + "batchId=" + batchId + ", currentQuantity=" + currentQuantity + ", currentSum=" + currentSum + super.toString() + '}';
    }

}
