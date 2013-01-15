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
public class BillDepositStart extends ActionState {

    public BillDepositStart(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
//        if (escrowDeposit) {
//            return "CONTINUE_DEPOSIT";
//        } else {
        return "IDLE";
    }

    @Override
    public void cancel() {
        stateApi.cancelTimer();
        stateApi.cancelDeposit();
    }

    @Override
    public void onGloryEvent(ManagerInterface.Status m) {
        switch (m.getState()) {
            case CANCELING:
                stateApi.setState(new Canceling(stateApi));
                break;
            case READY_TO_STORE:
                stateApi.setState(new BillDepositReadyToStore(stateApi));
                break;
            case ESCROW_FULL:
                stateApi.setState(new BillDepositReadyEscrowFull(stateApi));
                break;
            case COUNTING:
                //stateApi.cancelTimer();
                break;
            case PUT_THE_BILLS_ON_THE_HOPER:
                //stateApi.startTimer();
                break;
            case REMOVE_REJECTED_BILLS:
                stateApi.setState(new RemoveRejectedBills(stateApi, this));
                break;
            case JAM:
                stateApi.setState(new Jam(stateApi, this));
                break;
            case REMOVE_THE_BILLS_FROM_HOPER:
                break;
            case REMOVE_THE_BILLS_FROM_ESCROW:
                break;
            case INITIALIZING:
                break;
            default:
                Logger.debug("BillDepositStart onGloryEvent invalid state %s %s", m.name(), name());
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
