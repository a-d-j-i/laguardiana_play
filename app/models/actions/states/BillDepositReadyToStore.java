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
public class BillDepositReadyToStore extends ActionState {

    public BillDepositReadyToStore(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "READY_TO_STORE";
    }

    @Override
    public void cancel() {
        stateApi.cancelTimer();
        stateApi.cancelDeposit();
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
        Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
        switch (m.getState()) {
            case READY_TO_STORE:
                break;
            case CANCELING:
                stateApi.setState(new Canceling(stateApi));
                break;
            case COUNTING:
                stateApi.setState(new BillDepositStart(stateApi));
                break;
            case ESCROW_FULL:
                stateApi.setState(new BillDepositReadyEscrowFull(stateApi));
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
            default:
                Logger.debug("BillDepositReadyToStore onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }
}
