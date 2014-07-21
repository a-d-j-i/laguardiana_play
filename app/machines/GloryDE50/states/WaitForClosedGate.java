package machines.GloryDE50.states;

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
public class WaitForClosedGate extends MachineStateAbstract {

    protected final MachineStateInterface nextAction;

    public WaitForClosedGate(MachineStateInterface nextAction, MachineStateApiInterface machine) {
        super(machine);
        this.nextAction = nextAction;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MachineStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
//
//    @Override
//    public String getStateName() {
//        return "STORING";
//    }
    /*
     @Override
     public void onGloryEvent(ManagerInterface.ManagerStatus m) {
     Logger.error("ActionState invalid onGloryEvent %s", m.toString());
     }
     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     if (!Configuration.isIgnoreShutter()) {
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
     */

}
