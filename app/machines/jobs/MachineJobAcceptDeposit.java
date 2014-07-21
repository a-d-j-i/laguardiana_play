package machines.jobs;

import machines.MachineInterface;
import play.Logger;

/**
 *
 * @author adji
 */
final public class MachineJobAcceptDeposit extends MachineJob<Boolean> {

    public MachineJobAcceptDeposit(MachineInterface machine) {
        super(machine);
    }

    @Override
    public Boolean doJobWithResult() {
        Logger.debug("MachineJobAcceptDeposit");
        if (!machine.onAcceptDepositEvent()) {
            Logger.error("Can't accept deposit because the machine is not ready");
            return false;
        }
        return true;
    }

}
