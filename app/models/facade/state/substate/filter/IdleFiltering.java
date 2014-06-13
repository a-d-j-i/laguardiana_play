/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facade.state.substate.filter;

import models.facade.state.substate.ModelFacadeSubStateAbstract;
import models.facade.state.substate.Canceling;
import models.db.LgDeposit.FinishCause;
import models.facade.state.substate.ModelFacadeSubStateApi;

/**
 *
 * @author adji
 */
public class IdleFiltering extends ModelFacadeSubStateAbstract {

    public IdleFiltering(ModelFacadeSubStateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String getSubStateName() {
        return "IDLE";
    }
    /*
     @Override
     public void cancel() {
     stateApi.closeDeposit(FinishCause.FINISH_CAUSE_CANCEL);
     stateApi.cancelTimer();
     stateApi.cancelDeposit();
     stateApi.setState(new Canceling(stateApi));
     }*/
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
