/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facade.state.substate.bill_deposit;

import models.facade.state.substate.ModelFacadeSubStateAbstract;
import models.facade.state.substate.ModelFacadeSubStateApi;

/**
 *
 * @author adji
 */
public class BillDepositWithdraw extends ModelFacadeSubStateAbstract {

    public BillDepositWithdraw(ModelFacadeSubStateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String getSubStateName() {
        return "REMOVE_THE_BILLS_FROM_ESCROW";
    }
/*
    @Override
    public void onGloryEvent(ManagerStatus m) {
        Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
        switch (m.getState()) {
            case REMOVE_REJECTED_BILLS:
                break;
            case JAM:
                break;
            case REMOVE_THE_BILLS_FROM_ESCROW:
                break;
            case NEUTRAL:
            case INITIALIZING:
            case COUNTING:
                stateApi.setState(new BillDepositStart(stateApi));
            default:
                Logger.debug("Withdraw invalid state %s %s", m.name(), name());
                break;
        }
    }
    */
}
