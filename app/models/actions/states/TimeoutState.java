/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.ioboard.IoBoard;
import devices.glory.manager.ManagerInterface;
import models.actions.TimeoutTimer;
import models.actions.UserAction.StateApi;

/**
 *
 * @author adji
 */
public class TimeoutState extends ActionState {

    final protected ActionState returnState;

    public TimeoutState(StateApi stateApi, ActionState returnState) {
        super(stateApi);
        this.returnState = returnState;
    }

    @Override
    public String name() {
        return "TIMEOUT_WARNING";
    }

    @Override
        public void onGloryEvent(ManagerInterface.Status m) {
        suspendTimeout();
        returnState.onGloryEvent(m);
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        returnState.onIoBoardEvent(status);
    }

    @Override
    public void onTimeoutEvent(TimeoutTimer timer) {
        returnState.onTimeoutEvent(timer);
    }

    @Override
    public void suspendTimeout() {
        stateApi.restartTimer();
        stateApi.setState(returnState);
    }
}
