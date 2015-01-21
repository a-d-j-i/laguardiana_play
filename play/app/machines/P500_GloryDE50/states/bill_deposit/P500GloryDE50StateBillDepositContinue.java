package machines.P500_GloryDE50.states.bill_deposit;

import devices.device.status.DeviceStatusError;
import machines.P500_GloryDE50.states.context.P500GloryDE50StateBillDepositContext;
import devices.device.status.DeviceStatusInterface;
import devices.device.status.DeviceStatusStoringError;
import devices.glory.status.GloryDE50Status;
import devices.glory.status.GloryDE50StatusCurrentCount;
import devices.glory.status.GloryDE50StatusMachineErrorCode;
import machines.MachineDeviceDecorator;
import machines.P500_GloryDE50.states.P500GloryDE50StateError;
import machines.P500_GloryDE50.states.P500GloryDE50StateStoringError;
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
public class P500GloryDE50StateBillDepositContinue extends MachineStateAbstract {

    protected final P500GloryDE50StateBillDepositContext context;

    public P500GloryDE50StateBillDepositContinue(P500GloryDE50StateBillDepositContext context) {
        this.context = context;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(GloryDE50StatusMachineErrorCode.class)) {
            GloryDE50StatusMachineErrorCode e = (GloryDE50StatusMachineErrorCode) st;
            Logger.error("Machine on error 0x%x", e.getErrorCode());
            // TODO:
        } else if (st.is(GloryDE50Status.GloryDE50StatusType.COUNTING)) {
            context.setCurrentState(new P500GloryDE50StateBillDepositStart(context));
        } else if (st.is(GloryDE50Status.GloryDE50StatusType.PUT_THE_BILLS_ON_THE_HOPER)) {
            context.setCurrentState(new MachineStateAbstract() {
                @Override
                public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
                    if (st.is(GloryDE50Status.GloryDE50StatusType.COUNTING)) {
                        context.setCurrentState(P500GloryDE50StateBillDepositContinue.this);
                    }
                    P500GloryDE50StateBillDepositContinue.this.onDeviceEvent(dev, st);
                }

                @Override
                public MachineStatus getStatus() {
                    return P500GloryDE50StateBillDepositContinue.this.getStatus("PUT_THE_BILLS_ON_THE_HOPER");
                }

            });
            return;
        } else if (st.is(GloryDE50Status.GloryDE50StatusType.REMOVE_REJECTED_BILLS)) {
            context.setCurrentState(new MachineStateAbstract() {
                @Override
                public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
                    if (!st.is(GloryDE50Status.GloryDE50StatusType.REMOVE_REJECTED_BILLS)) {
                        context.setCurrentState(P500GloryDE50StateBillDepositContinue.this);
                    }
                    P500GloryDE50StateBillDepositContinue.this.onDeviceEvent(dev, st);
                }

                @Override
                public MachineStatus getStatus() {
                    return P500GloryDE50StateBillDepositContinue.this.getStatus("REMOVE_REJECTED_BILLS");
                }
            });
            return;
        } else if (st.is(GloryDE50Status.GloryDE50StatusType.JAM)) {
            context.setCurrentState(new MachineStateAbstract() {
                @Override
                public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
                    if (st.is(GloryDE50Status.GloryDE50StatusType.NEUTRAL)) {
                        context.setCurrentState(P500GloryDE50StateBillDepositContinue.this);
                    }
                    P500GloryDE50StateBillDepositContinue.this.onDeviceEvent(dev, st);
                }

                @Override
                public MachineStatus getStatus() {
                    return P500GloryDE50StateBillDepositContinue.this.getStatus("JAM");
                }
            });
            return;
        } else if (st.is(GloryDE50StatusCurrentCount.class)) {
            context.setCurrentQuantity((GloryDE50StatusCurrentCount) st);
            return;
        } else if (st.is(DeviceStatusError.class)) {
            DeviceStatusError se = (DeviceStatusError) st;
            context.setCurrentState(new P500GloryDE50StateError(context, this, se.getError()));
            return;
        } else if (st.is(DeviceStatusStoringError.class)) {
            DeviceStatusStoringError se = (DeviceStatusStoringError) st;
            context.setCurrentState(new P500GloryDE50StateStoringError(context, this, se.getError()));
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("CONTINUE_DEPOSIT");
    }

    public MachineBillDepositStatus getStatus(String stateName) {
        BillDeposit billDeposit = BillDeposit.findById(context.getDepositId());
        Long totalSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(context.getDepositId(), BillQuantity.getBillQuantities(billDeposit.currency, context.getCurrentQuantity(), null),
                context.getCurrentUserId(), "BillDepositController.mainloop", stateName, context.getCurrentSum(), totalSum);
    }

    /*
     @Override
     public void accept() {
     stateApi.closeDeposit(FinishCause.FINISH_CAUSE_OK);
     stateApi.setState(new Finish(stateApi));
     stateApi.cancelDeposit();
     }

     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
     cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
     }
     super.onIoBoardEvent(status);
     }
     */
    /*
     @Override
     public void onGloryEvent(ManagerInterface.ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case COUNTING:
     stateApi.setState(new BillDepositStart(stateApi));
     break;
     default:
     super.onGloryEvent(m);
     }
     }
     */
    @Override
    public String toString() {
        return "P500GloryDE50StateBillDepositContinue{" + "context=" + context.toString() + '}';
    }
}
