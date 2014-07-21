package machines.GloryDE50.states.filter;

import devices.device.status.DeviceStatusInterface;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.states.MachineStateApiInterface;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class IdleFiltering extends MachineStateAbstract {

    public IdleFiltering(MachineStateApiInterface machine) {
        super(machine);
    }

//    @Override
//    public String getStateName() {
//        return "IDLE";
//    }
    /*
     @Override
     public void cancel() {
     stateApi.closeDeposit(FinishCause.FINISH_CAUSE_CANCEL);
     stateApi.cancelTimer();
     stateApi.cancelDeposit();
     stateApi.setState(new Canceling(stateApi));
     }*/
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     switch (m.getState()) {
     case READY_TO_STORE:
     stateApi.setState(new ReadyToStoreCounting(stateApi));
     //startTimer(ActionState.READY_TO_STORE);
     break;
     case ESCROW_FULL:
     stateApi.withdraw();
     break;
     case COUNTING:
     //cancelTimer();
     break;
     case PUT_THE_BILLS_ON_THE_HOPER:
     //startTimer(ActionState.IDLE);
     break;
     case NEUTRAL:
     stateApi.setState(new Finish(stateApi));
     break;
     default:
     Logger.debug("IdleFiltering invalid state %s %s", m.name(), name());
     break;
     }
     }
     */
    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MachineStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
