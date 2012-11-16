/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.GloryManager;
import models.Configuration;
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
        switch (m.getState()) {
            case READY_TO_STORE:
                if (Configuration.ioBoardIgnore()) {
                    if (!stateApi.store()) {
                        Logger.error("startBillDeposit can't cancel glory");
                    }
                    stateApi.setState(new StoringEnvelopeDeposit(stateApi));
                } else {
                    stateApi.openGate();
                    stateApi.setState(new WaitForOpenGate(stateApi, new StoringEnvelopeDeposit(stateApi)));
                }
                break;
            /*            case STORING:
             stateApi.setState(new StoringEnvelopeDeposit(stateApi));
             break;*/
            default:
                Logger.debug("ReadyToStoreEnvelopeDeposit onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }
}
