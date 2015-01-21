package machines.jobs;

import machines.MachineInterface;
import play.Logger;

/**
 *
 * @author adji
 */
final public class MachineJobCancelDeposit extends MachineJob<Boolean> {

    public MachineJobCancelDeposit(MachineInterface machine) {
        super(machine);
    }

    @Override
    public Boolean doJobWithResult() {
        Logger.debug("MachineJobCancelDeposit");
        if (!machine.onCancelDeposit()) {
            Logger.error("Can't cancel deposit because the machine is not ready");
            return false;
        }
        return true;
    }

}
