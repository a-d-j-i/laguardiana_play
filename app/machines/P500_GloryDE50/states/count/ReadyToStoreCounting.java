package machines.P500_GloryDE50.states.count;

/**
 *
 * @author adji
 */
public class ReadyToStoreCounting extends IdleCounting {

//    @Override
//    public String getStateName() {
//        return "READY_TO_STORE";
//    }
    /*
     @Override
     public void accept() {
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
}
