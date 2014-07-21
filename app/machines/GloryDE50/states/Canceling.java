package machines.GloryDE50.states;

import devices.device.status.DeviceStatusInterface;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.states.MachineStateApiInterface;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class Canceling extends MachineStateAbstract {

    public Canceling(MachineStateApiInterface machine) {
        super(machine);
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
//        return "CANCELING";
//    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case NEUTRAL:
     stateApi.setState(new Finish(stateApi));
     break;
     case INITIALIZING:
     break;
     case CANCELING:
     break;
     case REMOVE_REJECTED_BILLS:
     break;
     case REMOVE_THE_BILLS_FROM_ESCROW:
     break;
     case REMOVE_THE_BILLS_FROM_HOPER:
     break;
     case JAM: //The door is jamed.
     stateApi.setError(ModelError.ERROR_CODE.ESCROW_JAMED, "Escrow jamed");
     break;
     default:
     Logger.debug("Canceling invalid state %s %s", m.name(), name());
     break;
     }
     }
     */

}
