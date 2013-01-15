/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface;
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
    public void onGloryEvent(ManagerInterface.Status m) {
        switch (m.getState()) {
            case PUT_THE_BILLS_ON_THE_HOPER:
                stateApi.closeDeposit();
                if (Configuration.ioBoardIgnore()) {
                    stateApi.setState(new Finish(stateApi));
                } else {
                    stateApi.closeGate();
                    stateApi.setState(new WaitForClosedGate(stateApi, new Finish(stateApi)));
                }
                stateApi.cancelDeposit();
                break;
            case COUNTING:
                stateApi.closeBatch();
                if (Configuration.ioBoardIgnore()) {
                    stateApi.setState(new BillDepositStart(stateApi));
                } else {
                    stateApi.closeGate();
                    stateApi.setState(new WaitForClosedGate(stateApi, new BillDepositStart(stateApi)));
                }
                break;
            case STORING:
                break;
            case REMOVE_REJECTED_BILLS:
                break;
            default:
                Logger.debug("StoringBillDeposit invalid state %s %s", m.name(), name());
                break;
        }
    }
}
