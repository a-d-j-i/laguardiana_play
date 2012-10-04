/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.GloryManager;
import devices.glory.manager.GloryManager.Status;
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
    public void onGloryEvent(Status m) {
        super.onGloryEvent(m);
        if (m != GloryManager.Status.IDLE) {
            Logger.debug("onGloryEvent invalid state %s %s", m.name(), name());
        }
        stateApi.setState(new ContinueDeposit(stateApi));
    }
}
