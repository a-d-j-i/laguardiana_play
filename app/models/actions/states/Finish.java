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
public class Finish extends ActionState {

    public Finish(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "FINISH";
    }

    @Override
    public void onGloryEvent(GloryManager.Status m) {
        super.onGloryEvent(m);
        if (m != GloryManager.Status.IDLE) {
            Logger.debug("onGloryEvent invalid state %s %s", m.name(), name());
        }
    }
}
