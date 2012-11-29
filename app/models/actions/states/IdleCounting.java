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
public class IdleCounting extends ActionState {

    public IdleCounting(StateApi stateApi) {
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
            case READY_TO_STORE:
                stateApi.setState(new ReadyToStoreCounting(stateApi));
                //startTimer(ActionState.READY_TO_STORE);
                break;
            case ESCROW_FULL:
                stateApi.withdraw();
                break;
            case COUNTING:
                //cancelTimer();
                break;
            case PUT_THE_BILLS_ON_THE_HOPER:
                //startTimer(ActionState.IDLE);
                break;
            case IDLE:
                stateApi.setState(new Finish(stateApi));
                break;
            default:
                Logger.debug("IdleCounting invalid state %s %s", m.name(), name());
                break;
        }
    }
}
