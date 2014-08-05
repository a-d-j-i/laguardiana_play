package machines.P500_MEI.states;

import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskWithdraw;
import devices.mei.status.MeiEbdsStatus;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateApiInterface;
import machines.states.MachineStateError;
import machines.status.MachineBillDepositStatus;
import models.BillDeposit;
import models.BillQuantity;
import models.db.LgDeposit.FinishCause;
import play.Logger;

/**
 * TODO: Move to MachineStateCanceling.
 *
 * @author adji
 */
public class P500MeiStateCanceling extends P500MeiStateBillDepositContinue {

    public P500MeiStateCanceling(MachineStateApiInterface machine, Integer currentUserId, Integer billDepositId, Integer batchId) {
        super(machine, currentUserId, billDepositId, batchId);
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
        if (st.is(MeiEbdsStatus.READY_TO_STORE)) {
            if (!dev.submitSynchronous(new DeviceTaskWithdraw())) {
                machine.setCurrentState(new MachineStateError(machine, "Error submitting withdraw"));
            }
            return;
        } else if (st.is(MeiEbdsStatus.COUNTING)) {
            if (!machine.cancel()) {
                Logger.error("Error calling machine.cancel");
            }
        } else if (st.is(MeiEbdsStatus.CANCELED)) {
            machine.setCurrentState(new P500MeiStateBillDepositFinish(machine, currentUserId, billDepositId, FinishCause.FINISH_CAUSE_CANCEL));
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        BillDeposit billDeposit = BillDeposit.findById(billDepositId);
        Long currentSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(billDeposit, BillQuantity.getBillQuantities(billDeposit.currency, billDeposit.getCurrentQuantity(), null),
                currentUserId, "BillDepositController.mainloop", "CANCELING", currentSum, currentSum);
    }

    @Override
    public String toString() {
        return "P500MeiStateCanceling{" + super.toString() + '}';
    }

}
