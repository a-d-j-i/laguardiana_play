package machines.P500_GloryDE50.states.bill_deposit;

import machines.P500_GloryDE50.states.context.P500GloryDE50StateBillDepositContext;
import machines.status.MachineBillDepositStatus;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateBillDepositReadyToStoreEscrowFull extends P500GloryDE50StateBillDepositContinue {

    private boolean delayedStore = false;

    public P500GloryDE50StateBillDepositReadyToStoreEscrowFull(P500GloryDE50StateBillDepositContext context) {
        super(context);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("ESCROW_FULL");
    }

    @Override
    public boolean onAcceptDepositEvent() {
//        closeBatch();
        return context.setCurrentState(new P500GloryDE50StateBillDepositStoring(context));
    }

    @Override
    public boolean onCancelDepositEvent() {
        return context.setCurrentState(new P500GloryDE50StateBillDepositWithdraw(context));
    }

    /*
     @Override
     public void cancel() {
     stateApi.cancelTimer();
     // Change to cancel to cancel the whole deposit
     stateApi.withdraw();
     }

     @Override
     public void accept() {
     if (!isReadyToAccept(false)) {
     return;
     }
     stateApi.cancelTimer();
     if (Configuration.isIgnoreShutter()) {
     if (!stateApi.store()) {
     Logger.error("startBillDeposit can't cancel glory");
     }
     stateApi.setState(new BillDepositStoringEscrowFull(stateApi));
     } else {
     stateApi.openGate();
     stateApi.setState(new WaitForOpenGate(stateApi, new BillDepositStoringEscrowFull(stateApi)));
     }
     }
     */
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case REMOVE_REJECTED_BILLS:
     break;
     case JAM:
     break;
     case ESCROW_FULL:
     if (delayedStore) {
     Logger.error("BillDepositReadyToStoreEscrowFull DELAYED STORE!!!");
     accept();
     }
     break;
     case CANCELING:
     stateApi.setState(new Canceling(stateApi));
     break;
     case REMOVE_THE_BILLS_FROM_ESCROW:
     stateApi.setState(new BillDepositWithdraw(stateApi));
     break;
     default:
     Logger.debug("BillDepositReadyEscrowFull onGloryEvent invalid state %s %s", m.name(), name());
     break;
     }
     }
     */
    /*
     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     Logger.error("BillDepositReadyToStoreEscrowFull onIoBoardEvent %s", status.toString());
     if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
     cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
     }
     if (delayedStore) {
     Logger.error("BillDepositReadyToStoreEscrowFull DELAYED STORE!!!");
     accept();
     }
     super.onIoBoardEvent(status);
     }
     */
    @Override
    public String toString() {
        return "P500GloryDE50StateBillDepositReadyToStoreEscrowFull{" + "context=" + context.toString() + '}';
    }
}
