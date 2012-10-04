/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.IoBoard.IoBoardStatus;
import devices.glory.manager.GloryManager;
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
        Logger.error("start Invalid step %s", name());
    }

    public void cancel() {
        Logger.error("cancel Invalid step %s", name());
    }

    public void accept() {
        Logger.error("accept Invalid step %s", name());
    }

    public void onGloryEvent(GloryManager.Status m) {
        Logger.debug("onGloryEvent %s %s", m.name(), name());
    }

    public void onIoBoardEvent(IoBoardStatus status) {
        Logger.debug("onIoBoardEvent %s %s", status.status.name(), name());
    }

    public void onTimeoutEvent(TimeoutTimer timer) {
        Logger.debug("onTimeoutEvent %s", name());
    }

    // used by timeout state.
    public void suspendTimeout() {
        Logger.debug("suspendTimeout %s", name());
    }

    public boolean canFinishAction() {
        return false;
    }
}
