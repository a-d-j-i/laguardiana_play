package machines.P500_MEI.states;

import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskReset;
import devices.device.task.DeviceTaskStore;
import devices.device.task.DeviceTaskWithdraw;
import devices.mei.task.MeiEbdsTaskCount;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import machines.MachineDeviceDecorator;
import machines.P500_MEI.MachineP500_MEI;
import machines.jobs.MachineJobCancelDeposit;
import machines.states.MachineStateContextInterface;
import machines.states.MachineStateInterface;
import models.BillDeposit;
import models.Configuration;
import models.ItemQuantity;
import models.db.LgBatch;
import models.db.LgDeposit;
import models.db.LgDeviceSlot;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MEIStateContext implements MachineStateContextInterface {

    private final MachineP500_MEI machine;
    private final MachineDeviceDecorator mei;
    private Integer depositId;
    private Integer currentUserId;
    private Integer batchId;

    public P500MEIStateContext(MachineP500_MEI machine, MachineDeviceDecorator mei) {
        this.machine = machine;
        this.mei = mei;
    }

    public boolean setCurrentState(MachineStateInterface prevState) {
        return machine.setCurrentState(prevState);
    }

    @Override
    public String toString() {
        return "P500MEIStateContext{" + "machine=" + machine + ", mei=" + mei + ", depositId=" + depositId + ", currentUserId=" + currentUserId + ", batchId=" + batchId + '}';
    }

    void clean() {
        Logger.debug("P500MEIStateContext Cleaning, old deposit %s", depositId);
        depositId = null;
        currentUserId = null;
        batchId = null;
    }

    public void setDeposit(LgDeposit dep) {
        Logger.debug("P500MEIStateContext Configuring deposit %s", depositId);
        this.depositId = dep.depositId;
        this.currentUserId = dep.user.userId;
    }

    public boolean count() {
        BillDeposit billDeposit = BillDeposit.findById(depositId);
        Map<String, Integer> slots = new HashMap<String, Integer>();
        for (LgDeviceSlot s : LgDeviceSlot.find(billDeposit.currency, mei.getLgDevice())) {
            slots.put(s.slot, null);
        }
        Logger.debug("Calling count on device %s, slots : %s", mei.toString(), slots.toString());
        return mei.submitSynchronous(new MeiEbdsTaskCount(slots));
    }

    public boolean cancel() {
        return mei.submitSynchronous(new DeviceTaskCancel());
    }

    boolean withdraw() {
        return mei.submitSynchronous(new DeviceTaskWithdraw());
    }

    boolean store() {
        return mei.submitSynchronous(new DeviceTaskStore(1));
    }

    boolean reset() {
        return mei.submitSynchronous(new DeviceTaskReset());
    }

    boolean isValidBill(String slot) {
        LgDeviceSlot s = LgDeviceSlot.find(mei.getLgDevice(), slot);
        if (s == null) {
            Logger.error("Error calling LgDeviceSlot.find for device %s, slot %s", mei.toString(), slot);
            return false;
        }
        BillDeposit billDeposit = BillDeposit.findById(depositId);
        boolean ret = (billDeposit.currency.equals(s.billType.currency));
        if (!ret) {
            Logger.warn("Invalid currency %s for slot %s, deposit currency %s",
                    s.billType.currency.toString(), slot, billDeposit.currency.toString());
        }
        return ret;
    }

    protected boolean addBillToDeposit(String slot) {
        LgBatch refBatch;
        if (batchId == null) {
            refBatch = new LgBatch(mei.getLgDevice());
            refBatch.save();
            batchId = refBatch.batchId;
        } else {
            refBatch = LgBatch.findById(batchId);
        }
        if (refBatch == null) {
            Logger.error("Error getting current batch id %d", batchId);
            return false;
        }
        LgDeviceSlot s = LgDeviceSlot.find(mei.getLgDevice(), slot);
        if (s == null) {
            Logger.error("Error calling LgDeviceSlot.find for device %s, slot %s", mei.toString(), slot);
            return false;
        }
        Logger.debug("Found the slot %s from device %s, addToDeposit", slot, mei.toString());
        BillDeposit billDeposit = BillDeposit.findById(depositId);
        if (!billDeposit.addBillToDeposit(refBatch, s.billType, 1)) {
            Logger.error("Error calling adding bill to deposit");
            return false;
        }
        ItemQuantity iq = billDeposit.bag.getItemQuantity();
        Long bagFreeSpace = Configuration.maxBillsPerBag() - Configuration.equivalentBillQuantity(iq.bills, iq.envelopes);
        if (bagFreeSpace <= 0) {
            Logger.debug("CANCEL, BAG FULL %s %s", iq.toString(), s.toString());
            machine.submit(new MachineJobCancelDeposit(machine, LgDeposit.FinishCause.FINISH_CAUSE_BAG_FULL));
        }
        return true;
    }

    protected void closeBatch() {
        // close current batch.
        LgBatch refBatch = null;
        if (batchId != null) {
            refBatch = LgBatch.findById(batchId);
        }
        if (refBatch != null && refBatch.finishDate == null) {
            refBatch.finishDate = new Date();
            refBatch.save();
        }
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public Integer getDepositId() {
        return depositId;
    }

}