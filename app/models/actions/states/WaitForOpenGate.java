/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.IoBoard;
import models.actions.UserAction.StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class WaitForOpenGate extends ActionState {

    protected final ActionState nextAction;

    public WaitForOpenGate(StateApi stateApi, ActionState nextAction) {
        super(stateApi);
        this.nextAction = nextAction;
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        switch (status.shutterState) {
            case SHUTTER_OPEN:
                if (!stateApi.store()) {
                    Logger.error("WaitForGate error calling store");
                }
                stateApi.setState(nextAction);
                break;
            case SHUTTER_CLOSED:
                stateApi.setError("WaitForGate shutter closed");
                break;
            default:
                Logger.debug("WaitForGate onIoBoardEvent invalid state %s %s", status.shutterState.name(), name());
                break;
        }
    }

    @Override
    public String name() {
        return nextAction.name();
    }
}
