/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.GloryManager;
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
    public void onGloryEvent(GloryManager.Status m) {
        super.onGloryEvent(m);
        switch (m.getState()) {
            case IDLE:
            case INITIALIZING:
                stateApi.clearError();
                stateApi.setState(new Finish(stateApi));
                break;
            case ERROR:
            default:
                Logger.debug("onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }
}
