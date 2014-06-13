/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facade.state.substate.count;

import devices.ioboard.IoBoard;
import models.Configuration;
import models.db.LgDeposit;
import models.facade.state.substate.ModelFacadeSubStateApi;

/**
 *
 * @author adji
 */
public class ReadyToStoreCounting extends IdleCounting {

    public ReadyToStoreCounting(ModelFacadeSubStateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String getSubStateName() {
        return "READY_TO_STORE";
    }
    /*
     @Override
     public void accept() {
     stateApi.cancelDeposit();
     }

     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
     cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
     }
     super.onIoBoardEvent(status);
     }
     */
}
