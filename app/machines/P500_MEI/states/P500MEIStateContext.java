package machines.P500_MEI.states;

import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskStore;
import devices.device.task.DeviceTaskWithdraw;
import devices.mei.task.MeiEbdsTaskCount;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import machines.MachineDeviceDecorator;
import machines.P500_MEI.MachineP500_MEI;
import machines.states.MachineStateContextInterface;
import machines.states.MachineStateInterface;
import models.BillDeposit;
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

    public boolean withdraw() {
        return mei.submitSynchronous(new DeviceTaskWithdraw());
    }

    boolean store() {
        return mei.submitSynchronous(new DeviceTaskStore(1));
    }

    boolean isValidBill(String slot) {
        LgDeviceSlot s = LgDeviceSlot.find(mei.getLgDevice(), slot);
        if (s == null) {
            Logger.error("Error calling LgDeviceSlot.find for device %s, slot %s", mei.toString(), slot);
            return false;
        }
        return true;
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
            Logger.error("Error gettong current batch id %d", batchId);
            return false;
        }
        LgDeviceSlot s = LgDeviceSlot.find(mei.getLgDevice(), slot);
        if (s == null) {
            Logger.error("Error calling LgDeviceSlot.find for device %s, slot %s", mei.toString(), slot);
            return false;
        }
        Logger.debug("Found the slot %s from device %s, addToDeposit", slot, mei.toString());
        BillDeposit billDeposit = BillDeposit.findById(depositId);
        return billDeposit.addBillToDeposit(refBatch, s.billType, 1);
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

    BillDeposit getBillDeposit() {
        return BillDeposit.findById(depositId);
    }

    public void setDeposit(LgDeposit dep) {
        if (dep != null) {
            this.depositId = dep.depositId;
            this.currentUserId = dep.user.userId;
        } else {
            this.depositId = null;
            this.currentUserId = null;
        }
    }

    void closeDeposit(LgDeposit.FinishCause finishCause) {
        closeBatch();
        BillDeposit billDeposit = null;
        if (depositId != null) {
            billDeposit = BillDeposit.findById(depositId);
        }
        if (billDeposit != null) {
            billDeposit.closeDeposit(finishCause);
        }
    }

}
