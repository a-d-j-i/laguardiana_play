package machines.P500_GloryDE50.states.envelope_deposit;

import machines.P500_GloryDE50.states.context.P500GloryDE50StateEnvelopeDepositContext;
import machines.status.MachineEnvelopeDepositStatus;

/**
 *
 * @author adji
 */
class P500GloryDE50StateEnvelopeDepositStoring extends P500GloryDE50StateEnvelopeDepositStart {

    public P500GloryDE50StateEnvelopeDepositStoring(P500GloryDE50StateEnvelopeDepositContext context) {
        super(context);
    }

    @Override
    public MachineEnvelopeDepositStatus getStatus() {
        return new MachineEnvelopeDepositStatus(context.getDepositId(), context.getCurrentUserId(), "EnvelopeDepositControler.mainloop", "STORING");
    }

//    @Override
//    public String getStateName() {
//        return "STORING";
//    }
    /*
     @Override
     public void cancel() {
     // this could happen on error
     cancelWithCause(FinishCause.FINISH_CAUSE_STORING_ERROR);
     }

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
    public String toString() {
        return "P500GloryDE50StateEnvelopeDepositStoring{" + "context=" + context.toString() + '}';
    }
}
