/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.GloryManager;
import models.actions.TimeoutTimer;
import models.actions.UserAction.StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class IdleBillDeposit extends ActionState {

    public IdleBillDeposit(StateApi stateApi) {
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
    public void onGloryEvent(GloryManager.Status m) {
        super.onGloryEvent(m);
        switch (m.getState()) {
            case READY_TO_STORE:
                stateApi.setState(new ReadyToStoreBillDeposit(stateApi));
                break;
            case ESCROW_FULL:
                stateApi.setState(new EscrowFullBillDeposit(stateApi));
                break;
            case IDLE:
                break;
            case COUNTING:
                stateApi.cancelTimer();
                break;
            case PUT_THE_BILLS_ON_THE_HOPER:
                stateApi.startTimer();
                break;
            case COUNT_DONE:
                stateApi.cancelTimer();
                stateApi.setState(new Finish(stateApi));
                break;
            default:
                Logger.debug("onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }

    @Override
    public void onTimeoutEvent(TimeoutTimer timer) {
        switch (timer.state) {
            case WARN:
                stateApi.setState(new TimeoutState(stateApi, this));
                break;
            case CANCEL:
            default:
                stateApi.setError("Timeout error need admin intervention");
                break;
        }

    }
}
