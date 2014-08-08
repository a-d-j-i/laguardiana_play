package machines.P500_GloryDE50.states.bill_deposit;

import machines.status.MachineBillDepositStatus;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateBillDepositReadyToStore extends P500GloryDE50StateBillDepositContinue {

    protected boolean delayedStore = false;

    public P500GloryDE50StateBillDepositReadyToStore(P500GloryDE50StateBillDepositContext context) {
        super(context);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("READY_TO_STORE");
    }

    @Override
    public boolean onCancelDepositEvent() {
        return context.setCurrentState(new P500GloryDE50StateBillDepositWithdraw(context));
    }

//
//    @Override
//    public String getStateName() {
//        return "READY_TO_STORE";
//    }
    /*
     @Override
     public void cancel() {
     cancelWithCause(FinishCause.FINISH_CAUSE_CANCEL);
     }

     @Override
     public void accept() {
     if (!isReadyToAccept(false)) {
     return;
     }
     stateApi.cancelTimer();
     if (Configuration.isIgnoreShutter()) {
     if (!stateApi.store()) {
     Logger.error("startBillDeposit can't deposit");
     }
     stateApi.setState(new BillDepositStoring(stateApi));
     } else {
     stateApi.openGate();
     stateApi.setState(new WaitForOpenGate(stateApi, new BillDepositStoring(stateApi)));
     }
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
     case READY_TO_STORE:
     if (delayedStore) {
     Logger.error("BillDepositReadyToStore DELAYED STORE!!!");
     accept();
     }
     break;
     case CANCELING:
     stateApi.setState(new Canceling(stateApi));
     break;
     case COUNTING:
     stateApi.setState(new BillDepositStart(stateApi));
     break;
     case ESCROW_FULL:
     stateApi.setState(new BillDepositReadyToStoreEscrowFull(stateApi));
     break;
     case PUT_THE_BILLS_ON_THE_HOPER:
     //stateApi.startTimer();
     break;
     default:
     Logger.debug("BillDepositReadyToStore onGloryEvent invalid state %s %s", m.name(), name());
     break;
     }
     }
     */
    /*
     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     Logger.error("ReadyToStoreEnvelopeDeposit onIoBoardEvent %s", status.toString());
     if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
     cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
     }
     if (delayedStore) {
     Logger.error("BillDepositReadyToStore DELAYED STORE!!!");
     accept();
     }
     super.onIoBoardEvent(status);
     }
     */
}
