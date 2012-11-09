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
class StoringEnvelopeDeposit extends ActionState {

    public StoringEnvelopeDeposit(StateApi stateApi) {
        super(stateApi);
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
            default:
                Logger.debug("onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }
}
