package machines.P500_GloryDE50.states.envelope_deposit;

import machines.states.MachineStateApiInterface;
import machines.status.MachineEnvelopeDepositStatus;
import models.EnvelopeDeposit;

/**
 *
 * @author adji
 */
class P500GloryDE50StateEnvelopeDepositStoring extends P500GloryDE50StateEnvelopeDepositStart {

    public P500GloryDE50StateEnvelopeDepositStoring(MachineStateApiInterface machine, Integer userId, Integer depositId) {
        super(machine, userId, depositId);
    }

    @Override
    public MachineEnvelopeDepositStatus getStatus() {
        EnvelopeDeposit envelopeDeposit = EnvelopeDeposit.findById(depositId);
        return new MachineEnvelopeDepositStatus(envelopeDeposit, userId, "EnvelopeDepositControler.mainloop", "STORING");
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
}
