/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.ioboard.IoBoard;
import devices.printer.PrinterStatus;
import models.ModelError;
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

    abstract public void onGloryEvent(ManagerStatus m);

    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        if (status.getShutterState() != IoBoard.SHUTTER_STATE.SHUTTER_CLOSED) {
            stateApi.setError(ModelError.ERROR_CODE.SHUTTER_NOT_CLOSED,
                    String.format("ActionState shutter open %s %s", status.getShutterState().name(), name()));
        }
    }

    public void onPrinterEvent(PrinterStatus status) {
        Logger.error("ActionState invalid onPrinterEvent %s", status.toString());
    }

    public void onTimeoutEvent(TimeoutTimer timer) {
        Logger.error("ActionState invalid onTimeoutEvent %s", name());
    }

    // used by timeout state.
    public void suspendTimeout() {
        Logger.debug("ActionState suspendTimeout %s", name());
    }

    public boolean canFinishAction() {
        return false;
    }
}
