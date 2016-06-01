package machines.jobs;

import machines.MachineInterface;
import play.Logger;

/**
 *
 * @author adji
 */
final public class MachineJobConfirmDeposit extends MachineJob<Boolean> {

    public MachineJobConfirmDeposit(MachineInterface machine) {
        super(machine);
    }

    @Override
    public Boolean doJobWithResult() {
        Logger.debug("MachineJobConfirmDeposit");
        if (!machine.onConfirmDeposit()) {
            Logger.error("Can't confirm deposit because the machine is not ready");
            return false;
        }
        return true;
    }

}
