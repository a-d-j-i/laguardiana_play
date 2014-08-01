package machines.states;

import devices.device.status.DeviceStatusInterface;
import machines.MachineDeviceDecorator;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class MachineStateBagRemoved extends MachineStateAbstract {

    final protected MachineStateAbstract prevState;

    public MachineStateBagRemoved(MachineStateAbstract prevState, MachineStateApiInterface machine) {
        super(machine);
        this.prevState = prevState;
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
//        return "BAG_REMOVED";
//    }
//    @Override
//    public boolean cancel() {
//        //stateApi.setState(prevState);
//        return prevState.cancel();
//    }

    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     prevState.onGloryEvent(m);
     }
     */
    /*
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
     */
}
