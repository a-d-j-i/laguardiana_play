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
public class ReadyToStoreEnvelopeDeposit extends IdleEnvelopeDeposit {

    public ReadyToStoreEnvelopeDeposit(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "READY_TO_STORE";
    }

    @Override
    public void accept() {
        stateApi.openDeposit();
        if (!stateApi.store()) {
            Logger.error("startEnvelopeDeposit can't cancel glory");
        }
    }

    @Override
    public void onGloryEvent(GloryManager.Status m) {
        super.onGloryEvent(m);
        switch (m) {
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
