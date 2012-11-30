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
public class IdleEnvelopeDeposit extends ActionState {

    public IdleEnvelopeDeposit(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "IDLE";
    }

    @Override
    public void cancel() {
        stateApi.cancelTimer();
        if (!stateApi.cancelDeposit()) {
            Logger.error("cancelDeposit can't cancel glory");
        }
        stateApi.setState(new Canceling(stateApi));
    }

    @Override
    public void onGloryEvent(ManagerInterface.Status m) {
        switch (m.getState()) {
            case IDLE:
                stateApi.envelopeDeposit();
                break;
            case PUT_THE_ENVELOPE_IN_THE_ESCROW:
                stateApi.setState(new ReadyToStoreEnvelopeDeposit(stateApi));
                break;
            default:
                Logger.debug("IdleEnvelopeDeposit onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }
}