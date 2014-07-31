package machines.states;

import devices.device.status.DeviceStatusInterface;
import machines.states.MachineStateAbstract;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateApiInterface;
import machines.states.MachineStateInterface;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class MachineStateWaitForOpenGate extends MachineStateAbstract {

    private boolean delayedStore = false;
    protected final MachineStateInterface nextAction;

    public MachineStateWaitForOpenGate(MachineStateInterface nextAction, MachineStateApiInterface machine) {
        super(machine);
        this.nextAction = nextAction;
    }

    /*
     @Override
     public void onGloryEvent(ManagerInterface.ManagerStatus m) {
     Logger.error("ActionState invalid onGloryEvent %s", m.toString());
     }
     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     Logger.error("WaitForOpenGate onIoBoardEvent %s", status.toString());
     if (!Configuration.isIgnoreShutter()) {
     switch (status.getShutterState()) {
     case SHUTTER_OPEN:
     if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
     if (!delayedStore) {
     delayedStore = true;
     stateApi.setState(new BagRemoved(stateApi, this));
     }
     } else {
     store();
     }
     break;
     case SHUTTER_CLOSED:
     stateApi.setError(ModelError.ERROR_CODE.SHUTTER_NOT_OPENING, "WaitForGate shutter closed");
     break;
     default:
     Logger.debug("WaitForGate onIoBoardEvent invalid state %s %s", status.getShutterState().name(), name());
     break;
     }
     }
     if (!Configuration.isIgnoreBag()) {
     if (status.getBagAproveState() == IoBoard.BAG_APROVE_STATE.BAG_APROVED) {
     if (delayedStore) {
     Logger.error("ReadyToStoreEnvelopeDeposit DELAYED STORE!!!");
     store();
     }
     }
     }
     }
     */
//    @Override
//    public String getStateName() {
//        return nextAction.getStateName();
//    }
    /*
     private void store() {
     if (!stateApi.store()) {
     Logger.error("WaitForGate error calling store");
     }
     stateApi.setState(nextAction);
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
