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
public class EscrowFullStoring extends ActionState {

    public EscrowFullStoring(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "ESCROW_FULL_STORING";
    }

    @Override
    public void onGloryEvent(GloryManager.Status m) {
        super.onGloryEvent(m);
        if (m.getState() != GloryManager.State.IDLE) {
            Logger.debug("onGloryEvent invalid state %s %s", m.name(), name());
        }
        stateApi.setState(new ContinueDeposit(stateApi));
    }
}
