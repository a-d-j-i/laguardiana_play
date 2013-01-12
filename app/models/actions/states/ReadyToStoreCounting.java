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
public class ReadyToStoreCounting extends IdleCounting {

    public ReadyToStoreCounting(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "READY_TO_STORE";
    }

    @Override
    public void accept() {
        if (!stateApi.cancelDeposit()) {
            Logger.error("startCounting can't cancel glory");
        }
    }
}
