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
        return "REMOVE_THE_BILLS_FROM_ESCROW";
    }

    @Override
    public void onGloryEvent(ManagerInterface.State m) {
        Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
        switch (m.getState()) {
            case REMOVE_REJECTED_BILLS:
                break;
            case JAM:
                break;
            case REMOVE_THE_BILLS_FROM_ESCROW:
                break;
            case NEUTRAL:
            case INITIALIZING:
            case COUNTING:
                stateApi.setState(new BillDepositStart(stateApi));
            default:
                Logger.debug("Withdraw invalid state %s %s", m.name(), name());
                break;
        }
    }
}
