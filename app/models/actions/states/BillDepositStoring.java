/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface.ManagerStatus;
import models.Configuration;
import models.actions.UserAction;
import play.Logger;

/**
 *
 * @author adji
 */
public class BillDepositStoring extends ActionState {

    public BillDepositStoring(UserAction.StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "STORING";
    }

    @Override
    public void onGloryEvent(ManagerStatus m) {
        Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
        switch (m.getState()) {
            case REMOVE_REJECTED_BILLS:
                stateApi.setState(new RemoveRejectedBills(stateApi, this));
                break;
            case JAM:
                stateApi.setState(new Jam(stateApi, this));
                break;
            case PUT_THE_BILLS_ON_THE_HOPER:
                stateApi.closeDeposit();
                if (Configuration.isIgnoreShutter()) {
                    stateApi.setState(new Finish(stateApi));
                } else {
                    stateApi.closeGate();
                    stateApi.setState(new WaitForClosedGate(stateApi, new Finish(stateApi)));
                }
                stateApi.cancelDeposit();
                break;
            case COUNTING:
                stateApi.closeBatch();
                if (Configuration.isIgnoreShutter()) {
                    stateApi.setState(new BillDepositStart(stateApi));
                } else {
                    stateApi.closeGate();
                    stateApi.setState(new WaitForClosedGate(stateApi, new BillDepositStart(stateApi)));
                }
                break;
            case STORING:
                break;
            default:
                Logger.debug("StoringBillDeposit invalid state %s %s", m.name(), name());
                break;
        }
    }
}
