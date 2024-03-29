/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.ioboard.IoBoard;
import models.Configuration;
import models.ModelError;
import models.actions.UserAction.StateApi;
import models.db.LgDeposit.FinishCause;
import play.Logger;

/**
 *
 * @author adji
 */
class EnvelopeDepositStoring extends ActionState {

    public EnvelopeDepositStoring(StateApi stateApi) {
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
            case NEUTRAL:
                stateApi.closeDeposit(FinishCause.FINISH_CAUSE_OK);
                if (Configuration.isIgnoreShutter()) {
                    stateApi.setState(new Finish(stateApi));
                } else {
                    stateApi.closeGate();
                    stateApi.setState(new WaitForClosedGate(stateApi, new Finish(stateApi)));
                }
                break;
            case INITIALIZING:
            case STORING:
            case READY_TO_STORE:
            case REMOVE_THE_BILLS_FROM_HOPER: // wait until this is done.
                break;
            case ERROR:
                cancelWithCause(FinishCause.FINISH_CAUSE_STORING_ERROR);
                break;
            default:
                Logger.debug("StoringEnvelopeDeposit invalid state %s %s", m.name(), name());
                break;
        }
    }

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
}
