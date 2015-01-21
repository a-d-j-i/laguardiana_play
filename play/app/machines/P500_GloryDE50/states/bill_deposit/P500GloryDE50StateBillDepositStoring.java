package machines.P500_GloryDE50.states.bill_deposit;

import machines.P500_GloryDE50.states.context.P500GloryDE50StateBillDepositContext;
import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskStore;
import devices.glory.status.GloryDE50Status;
import machines.MachineDeviceDecorator;
import machines.P500_GloryDE50.states.P500GloryDE50StateError;
import machines.status.MachineBillDepositStatus;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateBillDepositStoring extends P500GloryDE50StateBillDepositContinue {

    public P500GloryDE50StateBillDepositStoring(P500GloryDE50StateBillDepositContext context) {
        super(context);
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(GloryDE50Status.GloryDE50StatusType.ESCROW_FULL)
                || st.is(GloryDE50Status.GloryDE50StatusType.READY_TO_STORE)) {
            if (!dev.submitSynchronous(new DeviceTaskStore(1))) {
                context.setCurrentState(new P500GloryDE50StateError(context, this, "Error submitting store"));
                return;
            }
        }
        super.onDeviceEvent(dev, st);
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
    @Override
    public String toString() {
        return "P500GloryDE50StateBillDepositStoring{" + "context=" + context.toString() + '}';
    }
}
