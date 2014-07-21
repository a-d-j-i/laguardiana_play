package machines.P500_MEI.states;

import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskStore;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatusStored;
import java.util.Date;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.states.MachineStateApiInterface;
import machines.states.MachineStateError;
import machines.states.MachineStateJam;
import machines.status.MachineBillDepositStatus;
import models.BillDeposit;
import models.BillQuantity;
import models.db.LgBatch;
import models.db.LgDeviceSlot;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateBillDepositContinue extends MachineStateAbstract {

    protected final Integer currentUserId;
    protected final Integer billDepositId;
    protected Integer batchId = null;

    public P500MeiStateBillDepositContinue(MachineStateApiInterface machine, Integer currentUserId, Integer billDepositId, Integer batchId) {
        super(machine);
        this.currentUserId = currentUserId;
        this.billDepositId = billDepositId;
        this.batchId = batchId;
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
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        BillDeposit billDeposit = BillDeposit.findById(billDepositId);
        Long currentSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(billDeposit, BillQuantity.getBillQuantities(billDeposit.currency, billDeposit.getCurrentQuantity(), null),
                currentUserId, "BillDepositController.mainloop", "CONTINUE_DEPOSIT", "BillDepositMain, todo", currentSum, currentSum);
    }

    @Override
    public boolean onAcceptDepositEvent() {
        closeBatch();
        return machine.setCurrentState(new P500MeiStateAccepting(machine, currentUserId, billDepositId, batchId));
    }

    @Override
    public boolean onCancelDepositEvent() {
        closeBatch();
        return machine.setCurrentState(new P500MeiStateAccepting(machine, currentUserId, billDepositId, batchId));
    }

    @Override
    public String toString() {
        return "P500MeiStateBillDepositContinue{" + "currentUserId=" + currentUserId + ", billDepositId=" + billDepositId + ", batchId=" + batchId + '}';
    }

    protected boolean closeBatch() {
        // close current batch.
        LgBatch refBatch = null;
        if (batchId != null) {
            refBatch = LgBatch.findById(batchId);
        }
        if (refBatch != null) {
            refBatch.finishDate = new Date();
            refBatch.save();
            return true;
        }
        return false;
    }

    protected boolean addBillToDeposit(MachineDeviceDecorator dev, String slot) {
        LgBatch refBatch;
        if (batchId == null) {
            refBatch = new LgBatch(dev.getLgDevice());
            refBatch.save();
            batchId = refBatch.batchId;
        } else {
            refBatch = LgBatch.findById(batchId);
        }
        if (refBatch == null) {
            Logger.error("Error gettong current batch");
            return false;
        }
        LgDeviceSlot s = LgDeviceSlot.find(dev.getLgDevice(), slot);
        if (s == null) {
            Logger.error("Error calling LgDeviceSlot.find for device %s, slot %s", dev.toString(), slot);
            return false;
        }
        Logger.debug("Found the slot %s from device %s, addToDeposit", slot, dev.toString());
        BillDeposit billDeposit = BillDeposit.findById(billDepositId);
        return billDeposit.addBillToDeposit(refBatch, s.billType, 1);
    }

}
