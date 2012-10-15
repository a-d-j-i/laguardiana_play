/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.GloryManager;
import models.actions.UserAction;
import play.Logger;

/**
 *
 * @author adji
 */
public class StoringBillDeposit extends ActionState {

    final protected boolean escrowDeposit;

    public StoringBillDeposit(UserAction.StateApi stateApi, boolean escrowDeposit) {
        super(stateApi);
        this.escrowDeposit = escrowDeposit;
    }

    @Override
    public String name() {
        return "STORING";
    }

    @Override
    public void onGloryEvent(GloryManager.Status m) {
        super.onGloryEvent(m);
        switch (m.getState()) {
            case IDLE:
                stateApi.closeDeposit();
                stateApi.setState(new Finish(stateApi));
                break;
            case READY_TO_STORE:
            case COUNTING:
                stateApi.setState(new IdleBillDeposit(stateApi, escrowDeposit));
                break;
            case STORING:
                break;
            case REMOVE_REJECTED_BILLS:
                break;
            default:
                Logger.debug("onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }
}
