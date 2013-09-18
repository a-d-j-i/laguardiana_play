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
public class BillDepositContinue extends BillDepositStart {

    public BillDepositContinue(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "CONTINUE_DEPOSIT";
    }

    @Override
    public void accept() {
        stateApi.closeDeposit(false);
        stateApi.setState(new Finish(stateApi));
        stateApi.cancelDeposit();
    }

    @Override
    public void onGloryEvent(ManagerInterface.ManagerStatus m) {
        Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
        switch (m.getState()) {
            case COUNTING:
                stateApi.setState(new BillDepositStart(stateApi));
                break;
            default:
                super.onGloryEvent(m);
        }
    }
}
