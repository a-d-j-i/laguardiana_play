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

    public ReadyToStoreBillDeposit(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "READY_TO_STORE";
    }

    @Override
    public void accept() {
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
}
