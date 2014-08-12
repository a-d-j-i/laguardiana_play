package machines.P500_MEI.states;

import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskStore;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatusReadyToStore;
import devices.mei.status.MeiEbdsStatusStored;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.status.MachineBillDepositStatus;
import machines.status.MachineStatus;
import models.BillDeposit;
import models.BillQuantity;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateBillDepositContinue extends MachineStateAbstract {

    protected final P500MEIStateContext context;

    public P500MeiStateBillDepositContinue(P500MEIStateContext context) {
        this.context = context;
    }

    @Override
    public boolean onStart() {
        if (!context.count()) {
            Logger.error("Can't start MachineActionBillDeposit error in api.count");
            return false;
        }
        return true;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(MeiEbdsStatusReadyToStore.class)) {
            MeiEbdsStatusReadyToStore rts = (MeiEbdsStatusReadyToStore) st;
            if (context.isValidBill(rts.getSlot())) {
                if (!context.store()) {
                    context.setCurrentState(new P500MeiStateError(this, context.getCurrentUserId(), "Error submitting store"));
                }
            } else {
                Logger.debug("Invalid slot %s, withdraw", rts.getSlot());
                if (!context.withdraw()) {
                    context.setCurrentState(new P500MeiStateError(this, context.getCurrentUserId(), "Error submitting withdraw"));
                }
            }
            return;
        } else if (st.is(MeiEbdsStatusStored.class)) {
            MeiEbdsStatusStored stored = (MeiEbdsStatusStored) st;
            if (!context.addBillToDeposit(stored.getSlot())) {
                context.setCurrentState(new P500MeiStateError(this, context.getCurrentUserId(), "Error adding slot %s to batch", stored.getSlot()));
            }
            return;
        } else if (st.is(MeiEbdsStatus.JAM)) {
            context.setCurrentState(new MachineStateAbstract() {
                @Override
                public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
                    if (st.is(MeiEbdsStatus.NEUTRAL)) {
                        context.setCurrentState(P500MeiStateBillDepositContinue.this);
                    } else {
                        // TODO: Ignore ?
                        P500MeiStateBillDepositContinue.this.onDeviceEvent(dev, st);
                    }
                }

                @Override
                public MachineStatus getStatus() {
                    return P500MeiStateBillDepositContinue.this.getStatus("JAM");
                }

            });
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("CONTINUE_DEPOSIT");
    }

    public MachineBillDepositStatus getStatus(String stateName) {
        BillDeposit billDeposit = context.getBillDeposit();
        Long currentSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(billDeposit, BillQuantity.getBillQuantities(billDeposit.currency, billDeposit.getCurrentQuantity(), null),
                context.getCurrentUserId(), "BillDepositController.mainloop", stateName, null, currentSum);
    }

    @Override
    public boolean onAcceptDepositEvent() {
        context.closeBatch();
        return context.setCurrentState(new P500MeiStateAccepting(context));
    }

    @Override
    public boolean onCancelDepositEvent() {
        context.closeBatch();
        return context.setCurrentState(new P500MeiStateCanceling(context));
    }

    @Override
    public String toString() {
        return "P500MeiStateBillDepositContinue{" + context.toString() + '}';
    }

}
