package machines.P500_MEI.states;

import devices.device.status.DeviceStatusInterface;
import devices.mei.status.MeiEbdsStatus;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.states.MachineStateApiInterface;
import machines.status.MachineBillDepositStatus;
import models.BillDeposit;
import models.BillQuantity;
import models.db.LgDeposit.FinishCause;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateCanceling extends MachineStateAbstract {

    private final Integer currentUserId;
    private final Integer billDepositId;

    P500MeiStateCanceling(MachineStateApiInterface machine, Integer currentUserId, Integer billDepositId) {
        super(machine);
        this.currentUserId = currentUserId;
        this.billDepositId = billDepositId;
    }

    @Override
    public boolean onStart() {
        if (!machine.cancel()) {
            Logger.error("Error calling machine.cancel");
            return false;
        }
        return true;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(MeiEbdsStatus.CANCELED)) {
            machine.setCurrentState(new P500MeiStateFinish(machine, currentUserId, billDepositId, FinishCause.FINISH_CAUSE_CANCEL));
            return;
        } else if (st.is(MeiEbdsStatus.NEUTRAL)) {
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        BillDeposit billDeposit = BillDeposit.findById(billDepositId);
        Long currentSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(billDeposit, BillQuantity.getBillQuantities(billDeposit.currency, billDeposit.getCurrentQuantity(), null),
                currentUserId, "BillDepositController.mainloop", "CANCELING", "billdeposit.canceling", currentSum, currentSum);
    }

    @Override
    public String toString() {
        return "P500MeiStateCanceling{" + ", billDepositId=" + billDepositId + ", currentUserId=" + currentUserId + '}';
    }

}
