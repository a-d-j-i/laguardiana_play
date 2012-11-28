/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface;
import models.actions.TimeoutTimer;
import models.actions.UserAction.StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class IdleBillDeposit extends ActionState {

    final protected boolean escrowDeposit;

    public IdleBillDeposit(StateApi stateApi, boolean escrowDeposit) {
        super(stateApi);
        this.escrowDeposit = escrowDeposit;
    }

    @Override
    public String name() {
        if (escrowDeposit) {
            return "CONTINUE_DEPOSIT";
        } else {
            return "IDLE";
        }
    }

    @Override
    public void accept() {
        if (!escrowDeposit) {
            return;
        }
        stateApi.setState(new StoringBillDeposit(stateApi, escrowDeposit));
    }

    @Override
    public void cancel() {
        stateApi.cancelTimer();
        if (!stateApi.cancelDeposit()) {
            Logger.error("cancelDeposit can't cancel glory");
        }
        if (escrowDeposit) {
            stateApi.closeDeposit();
        }
        stateApi.setState(new Canceling(stateApi));
    }

    @Override
    public void onGloryEvent(ManagerInterface.Status m) {
        switch (m.getState()) {
            case READY_TO_STORE:
                stateApi.setState(new ReadyToStoreBillDeposit(stateApi, escrowDeposit, false));
                break;
            case ESCROW_FULL:
                stateApi.setState(new ReadyToStoreBillDeposit(stateApi, true, true));
                break;
            case IDLE:
                stateApi.closeDeposit();
                stateApi.setState(new Finish(stateApi));
                break;
            case COUNTING:
                //stateApi.cancelTimer();
                break;
            case PUT_THE_BILLS_ON_THE_HOPER:
                //stateApi.startTimer();
                break;
            case REMOVE_REJECTED_BILLS:
                stateApi.setState(new RemoveRejectedBillsBillDeposit(stateApi, this));
                break;
            default:
                Logger.debug("IdleBillDeposit onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }

    @Override
    public void onTimeoutEvent(TimeoutTimer timer) {
        /*        switch (timer.state) {
         case WARN:
         stateApi.setState(new TimeoutState(stateApi, this));
         break;
         case CANCEL:
         default:
         stateApi.setError("Timeout error need admin intervention");
         break;
         }
         */
    }
}
