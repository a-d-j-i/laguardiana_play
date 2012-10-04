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
public class Canceling extends ActionState {

    public Canceling(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "CANCELING";
    }

    @Override
    public void onGloryEvent(GloryManager.Status m) {
        super.onGloryEvent(m);
        switch (m) {
            case IDLE:
            case CANCELED:
                stateApi.setState(new Finish(stateApi));
                break;
            case REMOVE_REJECTED_BILLS:
                break;
            case REMOVE_THE_BILLS_FROM_ESCROW:
                break;
            case REMOVE_THE_BILLS_FROM_HOPER:
                break;
            default:
                Logger.debug("onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }
}
