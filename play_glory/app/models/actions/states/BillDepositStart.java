package models.actions.states;

import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.ioboard.IoBoard;
import models.actions.TimeoutTimer;
import models.actions.UserAction.StateApi;
import models.db.LgDeposit;
import models.db.LgDeposit.FinishCause;
import play.Logger;

/**
 *
 * @author adji
 */
public class BillDepositStart extends ActionState {

    public BillDepositStart(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
//        if (escrowDeposit) {
//            return "CONTINUE_DEPOSIT";
//        } else {
        return "IDLE";
    }

    @Override
    public void cancel() {
        if (stateApi.hasBills()) {
            cancelWithCause(FinishCause.FINISH_CAUSE_OK);
            // can be canceled after a full escrow deposit but without any bills in current batch
        } else {
            cancelWithCause(FinishCause.FINISH_CAUSE_CANCEL);
        }
    }

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
        /*        switch (timer.state) {
         case WARN:
         stateApi.setState(new TimeoutState(stateApi, this));
         break;
         case CANCEL:
         default:
         stateApi.setError("Timeout error need admin intervention");
         break;
         }
         */
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        if (!stateApi.isBagReady(false)) {
            cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
        }
        super.onIoBoardEvent(status);
    }
}
