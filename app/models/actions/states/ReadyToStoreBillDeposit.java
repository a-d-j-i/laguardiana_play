/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import models.Configuration;
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
        stateApi.cancelTimer();
        stateApi.addBatchToDeposit();
        if (Configuration.ioBoardIgnore()) {
            if (!stateApi.store()) {
                Logger.error("startBillDeposit can't cancel glory");
            }
            stateApi.setState(new StoringBillDeposit(stateApi, escrowDeposit));
        } else {
            stateApi.openGate();
            stateApi.setState(new WaitForOpenGate(stateApi, new StoringBillDeposit(stateApi, escrowDeposit)));
        }
    }
}
