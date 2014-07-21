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
class EnvelopeDepositStoring extends MachineStateAbstract {

    public EnvelopeDepositStoring(MachineStateApiInterface machine) {
        super(machine);
    }

//    @Override
//    public String getStateName() {
//        return "STORING";
//    }
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case NEUTRAL:
     stateApi.closeDeposit(FinishCause.FINISH_CAUSE_OK);
     if (Configuration.isIgnoreShutter()) {
     stateApi.setState(new Finish(stateApi));
     } else {
     stateApi.closeGate();
     stateApi.setState(new WaitForClosedGate(stateApi, new Finish(stateApi)));
     }
     break;
     case INITIALIZING:
     case STORING:
     break;
     default:
     Logger.debug("StoringEnvelopeDeposit invalid state %s %s", m.name(), name());
     break;
     }
     }
     */
    /*
     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     if (!Configuration.isIgnoreShutter()) {
     switch (status.getShutterState()) {
     case SHUTTER_OPEN:
     break;
     case SHUTTER_CLOSED:
     stateApi.setError(ModelError.ERROR_CODE.SHUTTER_NOT_OPENING, "WaitForGate shutter closed");
     break;
     default:
     Logger.debug("WaitForGate onIoBoardEvent invalid state %s %s", status.getShutterState().name(), name());
     break;
     }
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
