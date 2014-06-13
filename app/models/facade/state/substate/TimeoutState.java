/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facade.state.substate;

import devices.ioboard.IoBoard;

/**
 *
 * @author adji
 */
public class TimeoutState extends ModelFacadeSubStateAbstract {

    final protected ModelFacadeSubStateAbstract returnState;

    public TimeoutState(ModelFacadeSubStateAbstract returnState, ModelFacadeSubStateApi api) {
        super(api);
        this.returnState = returnState;
    }

    @Override
    public String getSubStateName() {
        return "TIMEOUT_WARNING";
    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     suspendTimeout();
     returnState.onGloryEvent(m);
     }
     */
    /*
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
     */
}
