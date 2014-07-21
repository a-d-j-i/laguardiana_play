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
public class BillDepositReadyToStore extends MachineStateAbstract {

    protected boolean delayedStore = false;

    public BillDepositReadyToStore(MachineStateApiInterface machine) {
        super(machine);
    }

//
//    @Override
//    public String getStateName() {
//        return "READY_TO_STORE";
//    }
    /*
     @Override
     public void cancel() {
     cancelWithCause(FinishCause.FINISH_CAUSE_CANCEL);
     }

     @Override
     public void accept() {
     if (!isReadyToAccept(false)) {
     return;
     }
     stateApi.cancelTimer();
     if (Configuration.isIgnoreShutter()) {
     if (!stateApi.store()) {
     Logger.error("startBillDeposit can't deposit");
     }
     stateApi.setState(new BillDepositStoring(stateApi));
     } else {
     stateApi.openGate();
     stateApi.setState(new WaitForOpenGate(stateApi, new BillDepositStoring(stateApi)));
     }
     }
     */
    /*
     @Override
     public void onGloryEvent(ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case REMOVE_REJECTED_BILLS:
     stateApi.setState(new RemoveRejectedBills(stateApi, this));
     break;
     case JAM:
     stateApi.setState(new Jam(stateApi, this));
     break;
     case READY_TO_STORE:
     if (delayedStore) {
     Logger.error("BillDepositReadyToStore DELAYED STORE!!!");
     accept();
     }
     break;
     case CANCELING:
     stateApi.setState(new Canceling(stateApi));
     break;
     case COUNTING:
     stateApi.setState(new BillDepositStart(stateApi));
     break;
     case ESCROW_FULL:
     stateApi.setState(new BillDepositReadyToStoreEscrowFull(stateApi));
     break;
     case PUT_THE_BILLS_ON_THE_HOPER:
     //stateApi.startTimer();
     break;
     default:
     Logger.debug("BillDepositReadyToStore onGloryEvent invalid state %s %s", m.name(), name());
     break;
     }
     }
     */
    /*
     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     Logger.error("ReadyToStoreEnvelopeDeposit onIoBoardEvent %s", status.toString());
     if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
     cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
     }
     if (delayedStore) {
     Logger.error("BillDepositReadyToStore DELAYED STORE!!!");
     accept();
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
