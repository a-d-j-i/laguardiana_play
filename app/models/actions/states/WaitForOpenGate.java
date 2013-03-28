/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface;
import devices.ioboard.IoBoard;
import models.ModelError;
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
    public void onGloryEvent(ManagerInterface.ManagerStatus m) {
        Logger.error("ActionState invalid onGloryEvent %s", m.toString());
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        switch (status.getShutterState()) {
            case SHUTTER_OPEN:
                if (!stateApi.store()) {
                    Logger.error("WaitForGate error calling store");
                }
                stateApi.setState(nextAction);
                break;
            case SHUTTER_CLOSED:
                stateApi.setError(ModelError.ERROR_CODE.SHUTTER_NOT_OPENING, "WaitForGate shutter closed");
                break;
            default:
                Logger.debug("WaitForGate onIoBoardEvent invalid state %s %s", status.getShutterState().name(), name());
                break;
        }
    }

    @Override
    public String name() {
        return nextAction.name();
    }
}
