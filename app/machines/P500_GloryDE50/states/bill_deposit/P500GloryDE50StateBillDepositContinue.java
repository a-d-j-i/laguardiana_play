package machines.P500_GloryDE50.states.bill_deposit;

import devices.device.status.DeviceStatusInterface;
import devices.glory.status.GloryDE50Status;
import devices.glory.status.GloryDE50StatusCurrentCount;
import java.util.Map;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.states.MachineStateApiInterface;
import machines.status.MachineBillDepositStatus;
import machines.status.MachineStatus;
import models.BillDeposit;
import models.BillQuantity;
import models.db.LgBillType;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateBillDepositContinue extends MachineStateAbstract {

    protected final P500GloryDE50StateBillDepositInfo info;

    public P500GloryDE50StateBillDepositContinue(MachineStateApiInterface machine, P500GloryDE50StateBillDepositInfo info) {
        super(machine);
        this.info = info;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(GloryDE50Status.GloryDE50StatusType.COUNTING)) {
            machine.setCurrentState(new P500GloryDE50StateBillDepositStart(machine, info));
        } else if (st.is(GloryDE50Status.GloryDE50StatusType.PUT_THE_BILLS_ON_THE_HOPER)) {
            machine.setCurrentState(new MachineStateAbstract(machine) {
                @Override
                public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
                    if (st.is(GloryDE50Status.GloryDE50StatusType.COUNTING)) {
                        machine.setCurrentState(P500GloryDE50StateBillDepositContinue.this);
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
            machine.setCurrentState(new MachineStateAbstract(machine) {
                @Override
                public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
                    if (!st.is(GloryDE50Status.GloryDE50StatusType.REMOVE_REJECTED_BILLS)) {
                        machine.setCurrentState(P500GloryDE50StateBillDepositContinue.this);
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
            machine.setCurrentState(new MachineStateAbstract(machine) {
                @Override
                public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
                    if (st.is(GloryDE50Status.GloryDE50StatusType.NEUTRAL)) {
                        machine.setCurrentState(P500GloryDE50StateBillDepositContinue.this);
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
            GloryDE50StatusCurrentCount currentCountStatus = (GloryDE50StatusCurrentCount) st;
            info.currentQuantity = dev.getQuantities(currentCountStatus.getCurrentQuantity());
            info.currentSum = 0L;
            for (Map.Entry<LgBillType, Integer> e : info.currentQuantity.entrySet()) {
                info.currentSum += (e.getValue() * e.getKey().denomination);
            }
            Logger.debug("Setting current count from : %s to %s", st.toString(), info.toString());
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("CONTINUE_DEPOSIT");
    }

    public MachineBillDepositStatus getStatus(String stateName) {
        BillDeposit billDeposit = BillDeposit.findById(info.billDepositId);
        Long totalSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(billDeposit, BillQuantity.getBillQuantities(billDeposit.currency, info.currentQuantity, null),
                info.currentUserId, "BillDepositController.mainloop", stateName, info.currentSum, totalSum);
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
}
