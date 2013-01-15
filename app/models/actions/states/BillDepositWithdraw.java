/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface;
import models.actions.UserAction.StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class BillDepositWithdraw extends ActionState {

    public BillDepositWithdraw(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "IDLE";
    }

    @Override
    public void onGloryEvent(ManagerInterface.Status m) {
        switch (m.getState()) {
            case NEUTRAL:
                stateApi.setState(new Finish(stateApi));
                break;
            case COUNTING:
                stateApi.setState(new BillDepositStart(stateApi));
            default:
                Logger.debug("Withdraw invalid state %s %s", m.name(), name());
                break;
        }
    }
}
