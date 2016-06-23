/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.ioboard.IoBoard;
import models.ModelError;
import models.actions.UserAction.StateApi;
import models.db.LgDeposit;
import models.db.LgDeposit.FinishCause;
import play.Logger;

/**
 *
 * @author adji
 */
public class EnvelopeDepositStart extends ActionState {

    public EnvelopeDepositStart(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "IDLE";
    }

    @Override
    public void cancel() {
        cancelWithCause(FinishCause.FINISH_CAUSE_CANCEL);
    }

    @Override
    public void onGloryEvent(ManagerStatus m) {
        Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
        switch (m.getState()) {
            case PUT_THE_ENVELOPE_IN_THE_ESCROW:
                stateApi.setState(new EnvelopeDepositReadyToStore(stateApi));
                break;
            case NEUTRAL:
            case CANCELING:
                stateApi.setState(new Canceling(stateApi));
                break;
            case REMOVE_REJECTED_BILLS:
                stateApi.setState(new RemoveRejectedBills(stateApi, this));
                break;
            case JAM:
                stateApi.setError(ModelError.ERROR_CODE.ESCROW_JAMED, "Escrow jamed");
                break;
            default:
                Logger.debug("EnvelopeDepositStart onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        if (!stateApi.isBagReady(true)) {
            cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
        }
        super.onIoBoardEvent(status);
    }
}
