/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.ioboard.IoBoard;
import models.Configuration;
import models.actions.UserAction.StateApi;
import models.db.LgDeposit.FinishCause;
import play.Logger;

/**
 *
 * @author adji
 */
public class BillDepositReadyToStore extends ActionState {

    protected boolean delayedStore = false;

    public BillDepositReadyToStore(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "READY_TO_STORE";
    }

    @Override
    public void cancel() {
        cancelWithCause(FinishCause.FINISH_CAUSE_CANCEL);
    }

    @Override
    public void cancelWithCause(FinishCause cause) {
        stateApi.closeDeposit(cause);
        stateApi.cancelTimer();
        stateApi.cancelDeposit();
    }

    @Override
    public void accept() {
        if (!isReadyToAccept(false)) {
            return;
        }
        stateApi.cancelTimer();
        stateApi.addBatchToDeposit();
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

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.error("ReadyToStoreEnvelopeDeposit onIoBoardEvent %s", status.toString());
        if (delayedStore) {
            Logger.error("BillDepositReadyToStore DELAYED STORE!!!");
            accept();
        }
        super.onIoBoardEvent(status);
    }
}
