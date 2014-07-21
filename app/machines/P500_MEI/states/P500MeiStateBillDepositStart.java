package machines.P500_MEI.states;

import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskStore;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatusStored;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateApiInterface;
import machines.states.MachineStateError;
import machines.states.MachineStateJam;
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
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(MeiEbdsStatus.READY_TO_STORE)) {
            if (!dev.submitSynchronous(new DeviceTaskStore(1))) {
                machine.setCurrentState(new MachineStateError(machine, "Error submitting store"));
            }
            return;
        } else if (st.is(MeiEbdsStatus.JAM)) {
            machine.setCurrentState(new MachineStateJam(machine, this, currentUserId, "BillDepositController"));
            return;
        } else if (st.is(MeiEbdsStatusStored.class)) {
            MeiEbdsStatusStored stored = (MeiEbdsStatusStored) st;
            if (!addBillToDeposit(dev, stored.getSlot())) {
                machine.setCurrentState(new MachineStateError(machine, "Error adding slot %s to batch", stored.getSlot()));
            }
            machine.setCurrentState(new P500MeiStateBillDepositContinue(machine, currentUserId, billDepositId, batchId));
            return;
        }
        super.onDeviceEvent(dev, st);
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
        return machine.setCurrentState(new P500MeiStateCanceling(machine, currentUserId, billDepositId));
    }

    @Override
    public String toString() {
        return "P500MeiStateBillDepositStart{" + super.toString() + '}';
    }

}
