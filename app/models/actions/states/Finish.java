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
public class Finish extends ActionState {

    public Finish(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "FINISH";
    }

    @Override
    public String getNeededActionAction() {
        return "finish";
    }

    @Override
    public boolean canFinishAction() {
        return true;
    }

    @Override
        public void onGloryEvent(ManagerInterface.Status m) {
        Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
        switch (m.getState()) {
            // Was canceled is ok
            case CANCELING:
                break;
            default:
                Logger.debug("Finish invalid state %s %s", m.name(), name());
                break;
        }
    }
}
