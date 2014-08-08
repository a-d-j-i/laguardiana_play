package machines.P500_GloryDE50.states.bill_deposit;

import machines.states.MachineStateApiInterface;
import machines.status.MachineBillDepositStatus;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateBillDepositStoring extends P500GloryDE50StateBillDepositContinue {

    public P500GloryDE50StateBillDepositStoring(MachineStateApiInterface machine, P500GloryDE50StateBillDepositInfo info) {
        super(machine, info);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("STORING");
    }

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
     case PUT_THE_BILLS_ON_THE_HOPER:
     stateApi.addBatchToDeposit(null);

     stateApi.closeDeposit(FinishCause.FINISH_CAUSE_OK);
     if (Configuration.isIgnoreShutter()) {
     stateApi.setState(new Finish(stateApi));
     } else {
     stateApi.closeGate();
     stateApi.setState(new WaitForClosedGate(stateApi, new Finish(stateApi)));
     }
     stateApi.cancelDeposit();
     break;
     case COUNTING:
     stateApi.addBatchToDeposit(null);

     stateApi.closeBatch();
     if (Configuration.isIgnoreShutter()) {
     stateApi.setState(new BillDepositStart(stateApi));
     } else {
     stateApi.closeGate();
     stateApi.setState(new WaitForClosedGate(stateApi, new BillDepositStart(stateApi)));
     }
     break;
     case STORING:
     break;
     default:
     Logger.debug("BillDepositStoring invalid state %s %s", m.name(), name());
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
}
