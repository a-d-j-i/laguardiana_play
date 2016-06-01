 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.ioboard.IoBoard;
import models.Configuration;
import models.ModelError;
import models.actions.UserAction;
import models.actions.UserAction.StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class BagRemoved extends ActionState {

    final protected ActionState prevState;

    public BagRemoved(StateApi stateApi, ActionState prevState) {
        super(stateApi);
        this.prevState = prevState;
    }

    @Override
    public String name() {
        return "BAG_REMOVED";
    }

    @Override
    public void cancel() {
        //stateApi.setState(prevState);
        prevState.cancel();
    }

    @Override
    public void onGloryEvent(ManagerStatus m) {
        prevState.onGloryEvent(m);
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.error("ActionState onIoBoardEvent %s", status.toString());
        if (!Configuration.isIgnoreShutter() && status.getShutterState() != IoBoard.SHUTTER_STATE.SHUTTER_CLOSED) {
            stateApi.setError(ModelError.ERROR_CODE.SHUTTER_NOT_CLOSED,
                    String.format("ActionState shutter open %s %s", status.getShutterState().name(), name()));
        }
        if (!Configuration.isIgnoreBag() && status.getBagAproveState() == IoBoard.BAG_APROVE_STATE.BAG_APROVED) {
            stateApi.setState(prevState);
        }
    }

    @Override
    public String getMessage(UserAction userAction) {
        return null;
        //return "application.bag_not_in_place";
    }
}
