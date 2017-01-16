package machines.P500_MEI.states;

import devices.device.status.DeviceStatusError;
import devices.device.status.DeviceStatusInterface;
import static devices.ioboard.response.IoboardStateResponse.BAG_STATE.BAG_STATE_INPLACE;
import devices.ioboard.status.IoBoardStatusError;
import devices.ioboard.status.IoboardStatus;
import devices.ioboard.status.IoboardStatus.IoboardBagApprovedState;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatusReadyToStore;
import devices.mei.status.MeiEbdsStatusStored;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.status.MachineBillDepositStatus;
import machines.status.MachineStatus;
import models.BillDeposit;
import models.BillQuantity;
import models.Configuration;
import models.db.LgDeposit;
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
        Logger.debug("P500MeiStateBillDepositContinue DEVICE EVENT %s, %s", dev.toString(), st.toString());
        if (st.is(IoboardStatus.class)) {
            IoboardStatus iobs = (IoboardStatus) st;
            if (iobs.getBagState() != BAG_STATE_INPLACE && !Configuration.isIgnoreBag()) {
                context.setCurrentState(new MachineStateAbstract() {
                    boolean delayedStore = false;

                    @Override
                    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
                        Logger.debug("BAG REMOVED DEVICE EVENT %s, %s", dev.toString(), st.toString());
                        if (st.is(MeiEbdsStatusReadyToStore.class)) {
                            MeiEbdsStatusReadyToStore rts = (MeiEbdsStatusReadyToStore) st;
                            if (context.isValidBill(rts.getSlot())) {
                                delayedStore = true;
                            } else {
                                Logger.error("Invalid slot %s, withdraw", rts.getSlot());
                                if (!context.withdraw()) {
                                    context.setCurrentState(new P500MeiStateError(this, context, "Error submitting withdraw"));
                                }
                            }
                        } else if (st.is(IoboardStatus.class)) {
                            IoboardStatus s = (IoboardStatus) st;
                            if (s.getBagApprovedState() == IoboardBagApprovedState.BAG_APROVED) {
                                if (delayedStore) {
                                    if (!context.store()) {
                                        context.setCurrentState(new P500MeiStateError(P500MeiStateBillDepositContinue.this, context, "Error submitting store"));
                                        return;
                                    }
                                }
                                context.setCurrentState(P500MeiStateBillDepositContinue.this);
                            }
                        } else {
                            P500MeiStateBillDepositContinue.this.onDeviceEvent(dev, st);
                        }
                    }

                    @Override
                    public MachineStatus getStatus() {
                        return P500MeiStateBillDepositContinue.this.getStatus("BAG_REMOVED");
                    }

                    @Override
                    public boolean onCancelDepositEvent(LgDeposit.FinishCause finishCause) {
                        return P500MeiStateBillDepositContinue.this.onCancelDepositEvent(finishCause);
                    }

                }
                );
            }
            return;
        } else if (st.is(MeiEbdsStatusReadyToStore.class)) {
            MeiEbdsStatusReadyToStore rts = (MeiEbdsStatusReadyToStore) st;
            if (context.isValidBill(rts.getSlot())) {
                if (!context.store()) {
                    context.setCurrentState(new P500MeiStateError(this, context, "Error submitting store"));
                }
            } else {
                Logger.error("Invalid slot %s, withdraw", rts.getSlot());
                if (!context.withdraw()) {
                    context.setCurrentState(new P500MeiStateError(this, context, "Error submitting withdraw"));
                }
            }
            return;
        } else if (st.is(MeiEbdsStatusStored.class)) {
            MeiEbdsStatusStored stored = (MeiEbdsStatusStored) st;
            if (!context.addBillToDeposit(stored.getSlot())) {
                context.setCurrentState(new P500MeiStateError(this, context, "Error adding slot %s to batch", stored.getSlot()));
            }
            return;
        } else if (st.is(MeiEbdsStatus.JAM)) {
            context.setCurrentState(new MachineStateAbstract() {
                @Override
                public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
                    Logger.debug("JAM DEVICE EVENT %s, %s", dev.toString(), st.toString());
                    if (st.is(MeiEbdsStatus.NEUTRAL) || st.is(MeiEbdsStatus.COUNTING)) {
                        context.setCurrentState(P500MeiStateBillDepositContinue.this);
                    } else if (st.is(MeiEbdsStatus.JAM)) {
                        // Ignore
                    } else {
                        P500MeiStateBillDepositContinue.this.onDeviceEvent(dev, st);
                    }
                }

                @Override
                public MachineStatus getStatus() {
                    return P500MeiStateBillDepositContinue.this.getStatus("JAM");
                }

            });
            return;
        } else if (st.is(DeviceStatusError.class)) {
            if (st.is(IoBoardStatusError.class)) {
                if (Configuration.isIgnoreIoBoard()) {
                    return;
                }
            }
            DeviceStatusError err = (DeviceStatusError) st;
            context.setCurrentState(new P500MeiStateError(this, context, err.getError()));
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("CONTINUE_DEPOSIT_NC");
    }

    public MachineBillDepositStatus getStatus(String stateName) {
        BillDeposit billDeposit = BillDeposit.findById(context.getDepositId());
        Long currentSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(context.getDepositId(), BillQuantity.getBillQuantities(billDeposit.currency, billDeposit.getCurrentQuantity(), null),
                context.getCurrentUserId(), "BillDepositController.mainloop", stateName, null, currentSum);
    }

    @Override
    public boolean onAcceptDepositEvent() {
        context.closeBatch();
        return context.setCurrentState(new P500MeiStateAccepting(context));
    }

    @Override
    public boolean onCancelDepositEvent(LgDeposit.FinishCause finishCause) {
        context.closeBatch();
        return context.setCurrentState(new P500MeiStateCanceling(context, finishCause));
    }

    @Override
    public String toString() {
        return "P500MeiStateBillDepositContinue{" + context.toString() + '}';
    }

}
