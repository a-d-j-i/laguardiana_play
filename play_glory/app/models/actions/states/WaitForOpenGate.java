/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface;
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
    protected final boolean envelope;

    public WaitForOpenGate(StateApi stateApi, ActionState nextAction, boolean envelope) {
        super(stateApi);
        this.nextAction = nextAction;
        this.envelope = envelope;
    }

    @Override
    public void onGloryEvent(ManagerInterface.ManagerStatus m) {
        Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
        switch (m.getState()) {
            case STORING:
                stateApi.addBatchToDeposit(m.getBills());
                stateApi.setState(nextAction);
                break;
            default:
                Logger.debug("WaitForOpenGate onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.error("WaitForOpenGate onIoBoardEvent %s", status.toString());
        if (!Configuration.isIgnoreShutter()) {
            switch (status.getShutterState()) {
                case SHUTTER_OPEN:
                    if (!stateApi.isBagReady(envelope)) {
                        if (!delayedStore) {
                            delayedStore = true;
                            stateApi.setState(new BagRemoved(stateApi, this, envelope));
                        }
                    } else {
                        store(envelope);
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
        if (stateApi.isBagReady(envelope)) {
            if (delayedStore) {
                Logger.error("ReadyToStoreEnvelopeDeposit DELAYED STORE!!!");
                store(envelope);
            }
        }
    }

    @Override
    public String name() {
        return nextAction.name();
    }

    private void store(boolean envelope) {
        if (!stateApi.store(envelope)) {
            Logger.error("WaitForGate error calling store");
        }
    }
}
