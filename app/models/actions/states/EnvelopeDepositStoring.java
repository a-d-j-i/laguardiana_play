/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface;
import models.Configuration;
import models.actions.UserAction.StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
class EnvelopeDepositStoring extends ActionState {

    public EnvelopeDepositStoring(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "STORING";
    }

    @Override
        public void onGloryEvent(ManagerInterface.Status m) {
        Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
        switch (m.getState()) {
            case NEUTRAL:
                stateApi.closeDeposit();
                if (Configuration.isIoBoardIgnore()) {
                    stateApi.setState(new Finish(stateApi));
                } else {
                    stateApi.closeGate();
                    stateApi.setState(new WaitForClosedGate(stateApi, new Finish(stateApi)));
                }
                break;
            case INITIALIZING:
            case STORING:
                break;
            default:
                Logger.debug("StoringEnvelopeDeposit invalid state %s %s", m.name(), name());
                break;
        }
    }
}
