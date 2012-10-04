/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import models.actions.UserAction.StateApi;

/**
 *
 * @author adji
 */
public class ContinueDeposit extends IdleBillDeposit {

    public ContinueDeposit(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "CONTINUE_DEPOSIT";
    }
}
