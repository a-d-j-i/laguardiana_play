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
public class BillDepositReadyEscrowFull extends ActionState {

    public BillDepositReadyEscrowFull(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "ESCROW_FULL";
    }

    @Override
    public void cancel() {
        stateApi.cancelTimer();
        // Change to cancel to cancel the whole deposit
        stateApi.withdraw();
    }

    @Override
    public void accept() {
        stateApi.cancelTimer();
        stateApi.addBatchToDeposit();
        if (Configuration.ioBoardIgnore()) {
            if (!stateApi.store()) {
                Logger.error("startBillDeposit can't cancel glory");
            }
            stateApi.setState(new BillDepositStoring(stateApi));
        } else {
            stateApi.openGate();
            stateApi.setState(new WaitForOpenGate(stateApi, new BillDepositStoring(stateApi)));
        }
    }

    @Override
    public void onGloryEvent(ManagerInterface.Status m) {
        switch (m.getState()) {
            case ESCROW_FULL:
                break;
            case CANCELING:
                stateApi.setState(new Canceling(stateApi));
                break;
            case REMOVE_THE_BILLS_FROM_ESCROW:
                stateApi.setState(new BillDepositWithdraw(stateApi));
                break;
            default:
                Logger.debug("BillDepositReadyEscrowFull onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }
}
