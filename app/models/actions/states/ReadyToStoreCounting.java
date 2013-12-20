/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.ioboard.IoBoard;
import models.Configuration;
import models.actions.UserAction.StateApi;
import models.db.LgDeposit;

/**
 *
 * @author adji
 */
public class ReadyToStoreCounting extends IdleCounting {

    public ReadyToStoreCounting(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "READY_TO_STORE";
    }

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
}
