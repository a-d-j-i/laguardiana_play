/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facade.state.substate.envelope_deposit;

import devices.ioboard.IoBoard;
import models.Configuration;
import models.facade.state.ModelFacadeStateAbstract;
import models.facade.state.substate.WaitForOpenGate;
import models.db.LgDeposit;
import models.facade.state.substate.ModelFacadeSubStateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class EnvelopeDepositReadyToStore extends EnvelopeDepositStart {

    private boolean delayedStore = false;

    public EnvelopeDepositReadyToStore(ModelFacadeSubStateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String getSubStateName() {
        return "READY_TO_STORE";
    }

    @Override
    public String getMessage(ModelFacadeStateAbstract userAction) {
        return "envelope_deposit.put_the_envelope_in_the_escrow";
    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case NEUTRAL:
     break;
     case CANCELING:
     stateApi.setState(new Canceling(stateApi));
     break;
     case READY_TO_STORE:
     if (isReadyToAccept(true)) {
     store();
     }
     break;
     case JAM: //The door is jamed.
     // Let it be!!!.
     stateApi.setState(new Jam(stateApi, this));
     //stateApi.setError(ModelError.ERROR_CODE.ESCROW_JAMED, "Escrow jamed");
     break;
     //                        case STORING:
     //             stateApi.setState(new StoringEnvelopeDeposit(stateApi));
     //             break;
     default:
     Logger.debug("EnvelopeDepositReadyToStore onGloryEvent invalid state %s %s", m.name(), name());
     break;
     }
     }
     */
    /*
     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     Logger.error("ReadyToStoreEnvelopeDeposit onIoBoardEvent %s", status.toString());
     if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
     cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
     }
     if (!Configuration.isIgnoreBag() && stateApi.isIoBoardOk()) {
     if (delayedStore) {
     Logger.error("ReadyToStoreEnvelopeDeposit DELAYED STORE!!!");
     store();
     }
     }
     super.onIoBoardEvent(status);
     }

     private void store() {
     if (Configuration.isIgnoreShutter()) {
     if (!stateApi.store()) {
     Logger.error("EnvelopeDepositReadyToStore can't store");
     }
     stateApi.setState(new EnvelopeDepositStoring(stateApi));
     } else {
     stateApi.openGate();
     stateApi.setState(new WaitForOpenGate(stateApi, new EnvelopeDepositStoring(stateApi)));
     }

     }
     */
}
