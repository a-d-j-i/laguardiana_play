/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface;
import devices.ioboard.IoBoard;
import models.Configuration;
import models.actions.UserAction;
import models.db.LgDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
public class BillDepositStoringEscrowFull extends BillDepositStoring {

    boolean mustCancelBagRemoved = false;

    public BillDepositStoringEscrowFull(UserAction.StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public void onGloryEvent(ManagerInterface.ManagerStatus m) {
        Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
        switch (m.getState()) {
            case REMOVE_REJECTED_BILLS:
                stateApi.setState(new RemoveRejectedBills(stateApi, this));
                break;
            case JAM:
                stateApi.setState(new Jam(stateApi, this));
                break;
            case PUT_THE_BILLS_ON_THE_HOPER:
            /*                stateApi.closeDeposit();
             if (Configuration.isIgnoreShutter()) {
             stateApi.setState(new Finish(stateApi));
             } else {
             stateApi.closeGate();
             stateApi.setState(new WaitForClosedGate(stateApi, new Finish(stateApi)));
             }
             stateApi.cancelDeposit();
             break;*/
            case NEUTRAL:
                stateApi.setState(new BillDepositContinue(stateApi));
                break;
            case COUNTING:
                stateApi.closeBatch();
                if (mustCancelBagRemoved) {
                    stateApi.setState(new Canceling(stateApi));
                    cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
                } else {
                    if (Configuration.isIgnoreShutter()) {
                        stateApi.setState(new BillDepositContinue(stateApi));
                    } else {
                        stateApi.closeGate();
                        stateApi.setState(new WaitForClosedGate(stateApi, new BillDepositContinue(stateApi)));
                    }
                }
                break;
            case STORING:
            case READY_TO_STORE: // aparentrly sometimes the escrow isn't full any more.
                break;
            default:
                Logger.debug("BillDepositStoringEscrowFull invalid state %s %s", m.name(), name());
                break;
        }
    }

    // Can't cancel now, but save the event.
    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.error("BillDepositReadyToStoreEscrowFull onIoBoardEvent %s", status.toString());
        if (!stateApi.isBagReady(false)) {
            mustCancelBagRemoved = true;
        }
        super.onIoBoardEvent(status);
    }
}
