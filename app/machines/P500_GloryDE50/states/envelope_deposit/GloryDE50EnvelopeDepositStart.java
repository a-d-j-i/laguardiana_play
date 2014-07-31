package machines.P500_GloryDE50.states.envelope_deposit;

import machines.states.MachineStateAbstract;
import machines.states.MachineStateApiInterface;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class GloryDE50EnvelopeDepositStart extends MachineStateAbstract {

    public GloryDE50EnvelopeDepositStart(MachineStateApiInterface machine, Integer userId, Integer depositId) {
        super(machine);
    }

//    @Override
//    public String getStateName() {
//        return "IDLE";
//    }

    /*    @Override
     public boolean cancel() {
     return api.cancelDeposit();
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
     case CANCELING:
     case INITIALIZING:
     stateApi.setState(new Canceling(stateApi));
     break;
     case READY_TO_STORE:
     stateApi.setState(new BillDepositReadyToStore(stateApi));
     break;
     case ESCROW_FULL:
     stateApi.setState(new BillDepositReadyToStoreEscrowFull(stateApi));
     break;
     case COUNTING:
     //stateApi.cancelTimer();
     break;
     case PUT_THE_BILLS_ON_THE_HOPER:
     //stateApi.startTimer();
     break;
     case REMOVE_THE_BILLS_FROM_HOPER:
     break;
     case REMOVE_THE_BILLS_FROM_ESCROW:
     break;
     case NEUTRAL:
     break;
     default:
     Logger.debug("%s onGloryEvent invalid state %s %s", this.getClass().getSimpleName(), m.name(), name());
     break;
     }
     }

     @Override
     public void onTimeoutEvent(TimeoutTimer timer) {
     switch (timer.state) {
     //         case WARN:
     //         stateApi.setState(new TimeoutState(stateApi, this));
     //         break;
     //         case CANCEL:
     //         default:
     //         stateApi.setError("Timeout error need admin intervention");
     //         break;
     //         }
     }

     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
     cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
     }
     super.onIoBoardEvent(status);
     }
     */
//    @Override
//    public String getNeededAction() {
//        return "mainloop";
//    }
//
//    @Override
//    public String getMessage() {
//        return "BillDepositStart, todo";
//    }
//    public void onDeviceEvent(DeviceEvent evt) {

    /*        DeviceStatusInterface st = evt.getStatus();
     if (st instanceof MeiEbdsStatus) {
     switch ((MeiEbdsStatus.MeiEbdsStatusType) st.getType()) {
     case READY_TO_STORE:
     break;
     default:
     Logger.debug("%s invalid event %s", toString(), evt.toString());
     break;
     }
     }*/
//        Logger.debug("%s ignore event : %s", toString(), evt.toString());
//    }
    @Override
    public String toString() {
        return "BillDepositStart";
    }

    @Override
    public MachineStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}