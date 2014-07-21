/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machines.GloryDE50.states.envelope_deposit;

import devices.device.status.DeviceStatusInterface;
import machines.MachineAbstract;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateAbstract;
import machines.states.MachineStateApiInterface;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class EnvelopeDepositStart extends MachineStateAbstract {

    public EnvelopeDepositStart(MachineStateApiInterface machine) {
        super(machine);
    }


    

//    @Override
//    public String getStateName() {
//        return "IDLE";
//    }

//    @Override
//    public boolean cancel() {
//        return api.cancelWithCause(FinishCause.FINISH_CAUSE_CANCEL);
//    }
//
//    @Override
//    public boolean accept() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case PUT_THE_ENVELOPE_IN_THE_ESCROW:
     stateApi.setState(new EnvelopeDepositReadyToStore(stateApi));
     break;
     case NEUTRAL:
     case CANCELING:
     stateApi.setState(new Canceling(stateApi));
     break;
     case REMOVE_REJECTED_BILLS:
     stateApi.setState(new RemoveRejectedBills(stateApi, this));
     break;
     case JAM:
     stateApi.setError(ModelError.ERROR_CODE.ESCROW_JAMED, "Escrow jamed");
     break;
     default:
     Logger.debug("EnvelopeDepositStart onGloryEvent invalid state %s %s", m.name(), name());
     break;
     }
     }

     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
     cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
     }
     super.onIoBoardEvent(status);
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
