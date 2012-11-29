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
public class ResetState extends ActionState {

    public ResetState(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "RESET";
    }

    @Override
    public void onGloryEvent(ManagerInterface.Status m) {
        switch (m.getState()) {
            case IDLE:
            case INITIALIZING:
                stateApi.clearError();
                stateApi.setState(new Finish(stateApi));
                break;
            case ERROR:
            default:
                Logger.debug("ResetState invalid state %s %s", m.name(), name());
                break;
        }
    }
}
