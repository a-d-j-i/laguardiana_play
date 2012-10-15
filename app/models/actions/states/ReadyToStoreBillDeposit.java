/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import models.actions.UserAction.StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class ReadyToStoreBillDeposit extends IdleBillDeposit {

    protected final boolean escrowFull;

    public ReadyToStoreBillDeposit(StateApi stateApi, boolean escrowDeposit, boolean escrowFull) {
        super(stateApi, escrowDeposit);
        this.escrowFull = escrowFull;
    }

    @Override
    public String name() {
        if (escrowFull) {
            return "ESCROW_FULL";
        } else {
            return "READY_TO_STORE";
        }
    }

    @Override
    public void accept() {
        // TODO: Do it right.
        //stateApi.openGate();
        stateApi.cancelTimer();
        stateApi.addBatchToDeposit();
        if (!stateApi.store()) {
            Logger.error("startBillDeposit can't cancel glory");
        }
        stateApi.setState(new StoringBillDeposit(stateApi, escrowDeposit));
    }
//    @Override
//    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
//        switch (status.status) {
//            case OPEN:
//                stateApi.addBatchToDeposit();
//                if (!stateApi.store()) {
//                    Logger.error("startBillDeposit can't cancel glory");
//                }
//                acceptNextStep();
//                break;
//            case OPENNING:
//                break;
//            default:
//                Logger.debug("onIoBoardEvent invalid state %s %s", status.status.name(), name());
//                break;
//        }
//    }
}
