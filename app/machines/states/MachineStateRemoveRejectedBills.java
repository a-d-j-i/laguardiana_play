package machines.states;

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
public class MachineStateRemoveRejectedBills extends MachineStateAbstract {

    final protected MachineStateInterface prevState;

    public MachineStateRemoveRejectedBills(MachineStateInterface prevState, MachineStateApiInterface machine) {
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
//        return "REMOVE_REJECTED_BILLS";
//    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     switch (m.getState()) {
     // Envelope deposit.
     case REMOVE_REJECTED_BILLS:
     break;
     case JAM:
     stateApi.setState(new Jam(stateApi, this));
     break;
     default:
     stateApi.setState(prevState);
     break;
     }
     }
     */

}
