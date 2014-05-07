/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.ioboard.IoBoard;
import models.Configuration;
import models.ModelError;
import models.actions.UserAction.StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class WaitForOpenGate extends ActionState {

    private boolean delayedStore = false;
    protected final ActionState nextAction;

    public WaitForOpenGate(StateApi stateApi, ActionState nextAction) {
        super(stateApi);
        this.nextAction = nextAction;
    }
/*
    @Override
    public void onGloryEvent(ManagerInterface.ManagerStatus m) {
        Logger.error("ActionState invalid onGloryEvent %s", m.toString());
    }
*/
    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.error("WaitForOpenGate onIoBoardEvent %s", status.toString());
        if (!Configuration.isIgnoreShutter()) {
            switch (status.getShutterState()) {
                case SHUTTER_OPEN:
                    if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
                        if (!delayedStore) {
                            delayedStore = true;
                            stateApi.setState(new BagRemoved(stateApi, this));
                        }
                    } else {
                        store();
                    }
                    break;
                case SHUTTER_CLOSED:
                    stateApi.setError(ModelError.ERROR_CODE.SHUTTER_NOT_OPENING, "WaitForGate shutter closed");
                    break;
                default:
                    Logger.debug("WaitForGate onIoBoardEvent invalid state %s %s", status.getShutterState().name(), name());
                    break;
            }
        }
        if (!Configuration.isIgnoreBag()) {
            if (status.getBagAproveState() == IoBoard.BAG_APROVE_STATE.BAG_APROVED) {
                if (delayedStore) {
                    Logger.error("ReadyToStoreEnvelopeDeposit DELAYED STORE!!!");
                    store();
                }
            }
        }
    }

    @Override
    public String name() {
        return nextAction.name();
    }

    private void store() {
        if (!stateApi.store()) {
            Logger.error("WaitForGate error calling store");
        }
        stateApi.setState(nextAction);
    }
}
