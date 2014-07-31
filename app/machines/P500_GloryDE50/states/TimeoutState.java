/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machines.P500_GloryDE50.states;

import devices.device.status.DeviceStatusInterface;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.states.MachineStateApiInterface;
import machines.states.MachineStateInterface;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class TimeoutState extends MachineStateAbstract {

    final protected MachineStateInterface returnState;

    public TimeoutState(MachineStateInterface returnState, MachineStateApiInterface machine) {
        super(machine);
        this.returnState = returnState;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MachineStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    @Override
//    public String getStateName() {
//        return "TIMEOUT_WARNING";
//    }
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
