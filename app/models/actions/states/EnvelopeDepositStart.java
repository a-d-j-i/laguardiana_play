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
public class EnvelopeDepositStart extends ActionState {

    public EnvelopeDepositStart(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "IDLE";
    }

    @Override
    public void cancel() {
        stateApi.cancelTimer();
        stateApi.cancelDeposit();
        stateApi.setState(new Canceling(stateApi));
    }

    @Override
    public void onGloryEvent(ManagerInterface.Status m) {
        switch (m.getState()) {
            case PUT_THE_ENVELOPE_IN_THE_ESCROW:
                stateApi.setState(new EnvelopeDepositReadyToStore(stateApi));
                break;
            case REMOVE_REJECTED_BILLS:
                stateApi.setState(new RemoveRejectedBills(stateApi, this));
                break;
            default:
                Logger.debug("IdleEnvelopeDeposit onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }
}
