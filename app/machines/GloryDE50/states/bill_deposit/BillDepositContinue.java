package machines.GloryDE50.states.bill_deposit;

import machines.states.MachineStateApiInterface;

/**
 *
 * @author adji
 */
public class BillDepositContinue extends GloryDE50BillDepositStart {

    public BillDepositContinue(MachineStateApiInterface machine, Integer userId, Integer depositId) {
        super(machine, userId, depositId);
    }

//    @Override
//    public String getStateName() {
//        return "CONTINUE_DEPOSIT";
//    }
    /*
     @Override
     public void accept() {
     stateApi.closeDeposit(FinishCause.FINISH_CAUSE_OK);
     stateApi.setState(new Finish(stateApi));
     stateApi.cancelDeposit();
     }

     @Override
     public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
     cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
     }
     super.onIoBoardEvent(status);
     }
     */
    /*
     @Override
     public void onGloryEvent(ManagerInterface.ManagerStatus m) {
     Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
     switch (m.getState()) {
     case COUNTING:
     stateApi.setState(new BillDepositStart(stateApi));
     break;
     default:
     super.onGloryEvent(m);
     }
     }
     */
}
