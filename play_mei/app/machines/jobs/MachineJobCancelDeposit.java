package machines.jobs;

import machines.MachineInterface;
import models.db.LgDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
final public class MachineJobCancelDeposit extends MachineJob<Boolean> {

    private final LgDeposit.FinishCause finishCause;

    public MachineJobCancelDeposit(MachineInterface machine, LgDeposit.FinishCause finishCause) {
        super(machine);
        this.finishCause = finishCause;
    }

    @Override
    public Boolean doJobWithResult() {
        Logger.debug("MachineJobCancelDeposit");
        if (!machine.onCancelDeposit(finishCause)) {
            Logger.error("Can't cancel deposit because the machine is not ready");
            return false;
        }
        return true;
    }

}
