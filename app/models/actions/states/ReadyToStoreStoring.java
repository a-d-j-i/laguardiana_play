/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.GloryManager;
import java.util.Date;
import models.Deposit;
import models.actions.UserAction;
import play.Logger;

/**
 *
 * @author adji
 */
public class ReadyToStoreStoring extends ActionState {

    public ReadyToStoreStoring(UserAction.StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "READY_TO_STORE_STORING";
    }

    @Override
    public void onGloryEvent(GloryManager.Status m) {
        super.onGloryEvent(m);
        if (m != GloryManager.Status.IDLE) {
            Logger.debug("onGloryEvent invalid state %s %s", m.name(), name());
        }
        stateApi.closeDeposit();
        stateApi.setState(new Finish(stateApi));
    }
}
