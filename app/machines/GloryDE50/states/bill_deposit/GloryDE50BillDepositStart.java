package machines.GloryDE50.states.bill_deposit;

import machines.states.MachineStateAbstract;
import machines.states.MachineStateApiInterface;
import machines.status.MachineBillDepositStatus;
import models.BillDeposit;
import models.BillQuantity;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50BillDepositStart extends MachineStateAbstract {

    protected final Integer currentUserId;
    protected final Integer billDepositId;
    protected Integer batchId = null;

    public GloryDE50BillDepositStart(MachineStateApiInterface machine, Integer currentUserId, Integer billDepositId) {
        super(machine);
        this.currentUserId = currentUserId;
        this.billDepositId = billDepositId;
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

//    @Override
//    public String getStateName() {
//        return "IDLE";
//    }

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
        return "BillDepositStart";
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        BillDeposit billDeposit = BillDeposit.findById(billDepositId);
        Long currentSum = billDeposit.getTotal();
        return new MachineBillDepositStatus(billDeposit, BillQuantity.getBillQuantities(billDeposit.currency, billDeposit.getCurrentQuantity(), null),
                currentUserId, "BillDepositController.mainloop", "CONTINUE_DEPOSIT", "BillDepositMain, todo", currentSum, currentSum);
    }

}
