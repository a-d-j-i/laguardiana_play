/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface;
import models.actions.TimeoutTimer;
import models.actions.UserAction.StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class RemoveRejectedBills extends ActionState {

    final protected ActionState prevState;

    public RemoveRejectedBills(StateApi stateApi, ActionState prevState) {
        super(stateApi);
        this.prevState = prevState;
    }

    @Override
    public String name() {
        return "REMOVE_REJECTED_BILLS";
    }

    @Override
    public void onGloryEvent(ManagerInterface.Status m) {
        switch (m.getState()) {
            // Envelope deposit.
            case PUT_THE_ENVELOPE_IN_THE_ESCROW:
            case IDLE:
                stateApi.setState(prevState);
                break;
            default:
                Logger.debug("RemoveRejectedBills onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }
}
