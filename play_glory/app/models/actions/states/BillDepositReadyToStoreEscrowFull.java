package models.actions.states;

import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.ioboard.IoBoard;
import models.Configuration;
import models.actions.UserAction.StateApi;
import models.db.LgDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
public class BillDepositReadyToStoreEscrowFull extends ActionState {

    private boolean delayedStore = false;

    public BillDepositReadyToStoreEscrowFull(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "ESCROW_FULL";
    }

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
            if (!stateApi.store(false)) {
                Logger.error("startBillDeposit can't cancel glory");
            }
        } else {
            stateApi.openGate();
            stateApi.setState(new WaitForOpenGate(stateApi, new BillDepositStoringEscrowFull(stateApi), false));
        }
    }

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
            case STORING:
                stateApi.addBatchToDeposit(m.getBills());
                stateApi.setState(new BillDepositStoringEscrowFull(stateApi));
                break;
            default:
                Logger.debug("BillDepositReadyEscrowFull onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.error("BillDepositReadyToStoreEscrowFull onIoBoardEvent %s", status.toString());
        if (!stateApi.isBagReady(false)) {
            cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
        }
        if (delayedStore) {
            Logger.error("BillDepositReadyToStoreEscrowFull DELAYED STORE!!!");
            accept();
        }
        super.onIoBoardEvent(status);
    }

}
