/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import models.actions.UserAction;
import models.actions.UserAction.StateApi;

/**
 *
 * @author adji
 */
public class EscrowFullBillDeposit extends ReadyToStoreBillDeposit {

    public EscrowFullBillDeposit(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "ESCROW_FULL";
    }

    @Override
    protected void acceptNextStep() {
        stateApi.setState(new EscrowFullStoring(stateApi));
    }
}
