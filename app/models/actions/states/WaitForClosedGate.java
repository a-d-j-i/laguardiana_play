/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface;
import devices.ioboard.IoBoard;
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
    public void onGloryEvent(ManagerInterface.ManagerStatus m) {
        Logger.error("ActionState invalid onGloryEvent %s", m.toString());
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        switch (status.getShutterState()) {
            case SHUTTER_CLOSED:
                stateApi.setState(nextAction);
                break;
            case SHUTTER_OPEN:
                break;
            default:
                Logger.error("WaitForGate onIoBoardEvent invalid state %s %s", status.getShutterState().name());
                break;
        }
    }
}
