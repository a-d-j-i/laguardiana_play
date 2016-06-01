package machines.P500_GloryDE50.states;

import machines.P500_GloryDE50.states.context.P500GloryDE50StateContext;
import devices.device.status.DeviceStatusInterface;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class P500GloryDE50Finish extends MachineStateAbstract {

    private final P500GloryDE50StateContext context;

    public P500GloryDE50Finish(P500GloryDE50StateContext context) {
        this.context = context;
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
//        return "FINISH";
//    }

//    @Override
//    public String getNeededActionAction() {
//        return "finish";
//    }
//
//    @Override
//    public boolean canFinishAction() {
//        return true;
//    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     // Was canceled is ok
     case CANCELING:
     break;
     // Ok, came here from previous state.
     case PUT_THE_BILLS_ON_THE_HOPER:
     break;
     default:
     Logger.debug("Finish invalid state %s %s", m.name(), name());
     break;
     }
     }
     */
}
