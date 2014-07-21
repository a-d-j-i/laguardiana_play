package machines.GloryDE50.states.count;

import machines.states.MachineStateApiInterface;

/**
 *
 * @author adji
 */
public class ReadyToStoreCounting extends IdleCounting {

    public ReadyToStoreCounting(MachineStateApiInterface machine) {
        super(machine);
    }

    
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
