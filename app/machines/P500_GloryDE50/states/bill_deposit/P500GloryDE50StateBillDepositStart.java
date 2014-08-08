package machines.P500_GloryDE50.states.bill_deposit;

import devices.device.status.DeviceStatusInterface;
import devices.glory.status.GloryDE50Status.GloryDE50StatusType;
import machines.MachineDeviceDecorator;
import machines.P500_GloryDE50.states.P500GloryDE50StateContext;
import machines.status.MachineBillDepositStatus;
import models.BillDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateBillDepositStart extends P500GloryDE50StateBillDepositContinue {

    public P500GloryDE50StateBillDepositStart(P500GloryDE50StateBillDepositContext context) {
        super(context);
    }

    public P500GloryDE50StateBillDepositStart(P500GloryDE50StateContext context) {
        super(new P500GloryDE50StateBillDepositContext(context));
    }

//    @Override
//    public boolean onStart() {
//        BillDeposit billDeposit = BillDeposit.findById(info.billDepositId);
//        if (!machine.count(billDeposit.currency)) {
//            Logger.error("Can't start P500GloryDE50StateBillDepositStart error in api.count");
//            return false;
//        }
//        return true;
//    }
    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(GloryDE50StatusType.NEUTRAL)) {
            if (!context.count()) {
                Logger.error("Can't start P500GloryDE50StateBillDepositStart error in api.count");
            }
            return;
        } else if (st.is(GloryDE50StatusType.READY_TO_STORE)) {
            context.setCurrentState(new P500GloryDE50StateBillDepositReadyToStore(context));
        } else if (st.is(GloryDE50StatusType.ESCROW_FULL)) {
            context.setCurrentState(new P500GloryDE50StateBillDepositReadyToStoreEscrowFull(context));
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("IDLE");
    }

    /*    @Override
     public boolean cancel() {
     return api.cancelDeposit();
     }
     */
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case REMOVE_REJECTED_BILLS:
     stateApi.setState(new RemoveRejectedBills(stateApi, this));
     break;
     case JAM:
     stateApi.setState(new Jam(stateApi, this));
     break;
     case CANCELING:
     case INITIALIZING:
     stateApi.setState(new Canceling(stateApi));
     break;
     case READY_TO_STORE:
     stateApi.setState(new BillDepositReadyToStore(stateApi));
     break;
     case ESCROW_FULL:
     stateApi.setState(new BillDepositReadyToStoreEscrowFull(stateApi));
     break;
     case COUNTING:
     //stateApi.cancelTimer();
     break;
     case PUT_THE_BILLS_ON_THE_HOPER:
     //stateApi.startTimer();
     break;
     case REMOVE_THE_BILLS_FROM_HOPER:
     break;
     case REMOVE_THE_BILLS_FROM_ESCROW:
     break;
     case NEUTRAL:
     break;
     default:
     Logger.debug("%s onGloryEvent invalid state %s %s", this.getClass().getSimpleName(), m.name(), name());
     break;
     }
     }

     @Override
     public void onTimeoutEvent(TimeoutTimer timer) {
     switch (timer.state) {
     //         case WARN:
     //         stateApi.setState(new TimeoutState(stateApi, this));
     //         break;
     //         case CANCEL:
     //         default:
     //         stateApi.setError("Timeout error need admin intervention");
     //         break;
     //         }
     }

     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
     cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
     }
     super.onIoBoardEvent(status);
     }
     */
//    @Override
//    public String getNeededAction() {
//        return "mainloop";
//    }
//
//    @Override
//    public String getMessage() {
//        return "BillDepositStart, todo";
//    }
//    public void onDeviceEvent(DeviceEvent evt) {

    /*        DeviceStatusInterface st = evt.getStatus();
     if (st instanceof MeiEbdsStatus) {
     switch ((MeiEbdsStatus.MeiEbdsStatusType) st.getType()) {
     case READY_TO_STORE:
     break;
     default:
     Logger.debug("%s invalid event %s", toString(), evt.toString());
     break;
     }
     }*/
//        Logger.debug("%s ignore event : %s", toString(), evt.toString());
//    }
    @Override
    public String toString() {
        return "P500GloryDE50StateBillDepositStart";
    }

}
