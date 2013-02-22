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
    public void onGloryEvent(ManagerInterface.State m) {
        switch (m.getState()) {
            // Envelope deposit.
            case REMOVE_REJECTED_BILLS:
                break;
            case JAM:
                stateApi.setState(new Jam(stateApi, this));
                break;
            default:
                stateApi.setState(prevState);
                break;
        }
    }
}
