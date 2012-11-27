/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.IoBoard;
import devices.glory.manager.ManagerInterface;
import models.actions.TimeoutTimer;
import models.actions.UserAction.StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class ActionState {

    final protected StateApi stateApi;

    public ActionState(StateApi stateApi) {
        this.stateApi = stateApi;
    }

    abstract public String name();

    public String getNeededActionAction() {
        return "mainLoop";
    }

    public void start() {
        Logger.error("ActionState start Invalid step %s", name());
    }

    public void cancel() {
        Logger.error("ActionState cancel Invalid step %s", name());
    }

    public void accept() {
        Logger.error("ActionState accept Invalid step %s", name());
    }

    public void onGloryEvent(ManagerInterface.Status m) {
        Logger.debug("ActionState onGloryEvent %s %s", m.name(), name());
    }

    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.debug("ActionState onIoBoardEvent %s", status.toString());
    }

    public void onTimeoutEvent(TimeoutTimer timer) {
        Logger.debug("ActionState onTimeoutEvent %s", name());
    }

    // used by timeout state.
    public void suspendTimeout() {
        Logger.debug("ActionState suspendTimeout %s", name());
    }

    public boolean canFinishAction() {
        return false;
    }
}
