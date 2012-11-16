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
public class WaitForClosedGate extends ActionState {

    protected final ActionState nextAction;

    public WaitForClosedGate(StateApi stateApi, ActionState nextAction) {
        super(stateApi);
        this.nextAction = nextAction;
    }

    @Override
    public String name() {
        return "STORING";
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        switch (status.shutterState) {
            case SHUTTER_CLOSED:
                stateApi.setState(nextAction);
                break;
            default:
                Logger.debug("WaitForGate onIoBoardEvent invalid state %s %s", status.shutterState.name());
                break;
        }
    }
}
