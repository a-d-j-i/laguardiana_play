package models.facade.state.substate.bill_deposit;

import devices.ioboard.IoBoard;
import models.Configuration;
import models.facade.state.substate.Finish;
import models.db.LgDeposit;
import models.db.LgDeposit.FinishCause;
import models.facade.state.substate.ModelFacadeSubStateApi;

/**
 *
 * @author adji
 */
public class BillDepositContinue extends BillDepositStart {

    public BillDepositContinue(ModelFacadeSubStateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String getSubStateName() {
        return "CONTINUE_DEPOSIT";
    }
    /*
     @Override
     public void accept() {
     stateApi.closeDeposit(FinishCause.FINISH_CAUSE_OK);
     stateApi.setState(new Finish(stateApi));
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
    /*
     @Override
     public void onGloryEvent(ManagerInterface.ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case COUNTING:
     stateApi.setState(new BillDepositStart(stateApi));
     break;
     default:
     super.onGloryEvent(m);
     }
     }
     */
}
