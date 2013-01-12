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
public class Jam extends ActionState {

    final protected ActionState prevState;

    public Jam(StateApi stateApi, ActionState prevState) {
        super(stateApi);
        this.prevState = prevState;
    }

    @Override
    public String name() {
        return "JAM";
    }

    @Override
    public void onGloryEvent(ManagerInterface.Status m) {
        switch (m.getState()) {
            case IDLE:
                stateApi.setState(prevState);
                break;
            default:
                Logger.debug("RemoveRejectedBills onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }
}
