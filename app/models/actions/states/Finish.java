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
    public void onGloryEvent(ManagerInterface.Status m) {
        if (m.getState() != ManagerInterface.State.IDLE) {
            Logger.debug("Finish invalid state %s %s", m.name(), name());
        }
    }

    @Override
    public String getNeededActionAction() {
        return "finish";
    }

    @Override
    public boolean canFinishAction() {
        return true;
    }
}
