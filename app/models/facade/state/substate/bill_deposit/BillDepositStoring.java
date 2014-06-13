/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facade.state.substate.bill_deposit;

import devices.ioboard.IoBoard;
import models.Configuration;
import models.ModelError;
import models.facade.state.substate.ModelFacadeSubStateAbstract;
import models.facade.state.substate.ModelFacadeSubStateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class BillDepositStoring extends ModelFacadeSubStateAbstract {

    public BillDepositStoring(ModelFacadeSubStateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String getSubStateName() {
        return "STORING";
    }
    /*
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
     stateApi.addBatchToDeposit(null);

     stateApi.closeDeposit(FinishCause.FINISH_CAUSE_OK);
     if (Configuration.isIgnoreShutter()) {
     stateApi.setState(new Finish(stateApi));
     } else {
     stateApi.closeGate();
     stateApi.setState(new WaitForClosedGate(stateApi, new Finish(stateApi)));
     }
     stateApi.cancelDeposit();
     break;
     case COUNTING:
     stateApi.addBatchToDeposit(null);

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
     Logger.debug("BillDepositStoring invalid state %s %s", m.name(), name());
     break;
     }
     }
     */
    /*
     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     if (!Configuration.isIgnoreShutter()) {
     switch (status.getShutterState()) {
     case SHUTTER_OPEN:
     break;
     case SHUTTER_CLOSED:
     stateApi.setError(ModelError.ERROR_CODE.SHUTTER_NOT_OPENING, "WaitForGate shutter closed");
     break;
     default:
     Logger.debug("WaitForGate onIoBoardEvent invalid state %s %s", status.getShutterState().name(), name());
     break;
     }
     }
     }
     */
}
