/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.IoBoard;
import models.actions.UserAction.StateApi;
import play.Logger;
import play.Play;

/**
 *
 * @author adji
 */
public class ReadyToStoreBillDeposit extends IdleBillDeposit {

    boolean openGate = false;

    public ReadyToStoreBillDeposit(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "READY_TO_STORE";
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
        acceptNextStep();
    }

    protected void acceptNextStep() {
        stateApi.setState(new ReadyToStoreStoring(stateApi));
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
