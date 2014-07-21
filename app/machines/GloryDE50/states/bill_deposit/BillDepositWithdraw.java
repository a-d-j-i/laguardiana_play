/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machines.GloryDE50.states.bill_deposit;

import devices.device.status.DeviceStatusInterface;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.states.MachineStateApiInterface;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class BillDepositWithdraw extends MachineStateAbstract {

    public BillDepositWithdraw(MachineStateApiInterface machine) {
        super(machine);
    }

//    @Override
//    public String getStateName() {
//        return "REMOVE_THE_BILLS_FROM_ESCROW";
//    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case REMOVE_REJECTED_BILLS:
     break;
     case JAM:
     break;
     case REMOVE_THE_BILLS_FROM_ESCROW:
     break;
     case NEUTRAL:
     case INITIALIZING:
     case COUNTING:
     stateApi.setState(new BillDepositStart(stateApi));
     default:
     Logger.debug("Withdraw invalid state %s %s", m.name(), name());
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
