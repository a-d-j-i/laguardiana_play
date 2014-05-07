/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import models.actions.UserAction.StateApi;
import models.db.LgDeposit.FinishCause;
import play.Logger;

/**
 *
 * @author adji
 */
public class IdleFiltering extends ActionState {

    public IdleFiltering(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "IDLE";
    }

    @Override
    public void cancel() {
        stateApi.closeDeposit(FinishCause.FINISH_CAUSE_CANCEL);
        stateApi.cancelTimer();
        stateApi.cancelDeposit();
        stateApi.setState(new Canceling(stateApi));
    }
/*
    @Override
    public void onGloryEvent(ManagerStatus m) {
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
            case NEUTRAL:
                stateApi.setState(new Finish(stateApi));
                break;
            default:
                Logger.debug("IdleFiltering invalid state %s %s", m.name(), name());
                break;
        }
    }
    */
}
