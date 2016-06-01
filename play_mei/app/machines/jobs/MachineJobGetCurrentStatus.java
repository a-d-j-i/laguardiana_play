package machines.jobs;

import machines.MachineInterface;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
final public class MachineJobGetCurrentStatus extends MachineJob<MachineStatus> {

    public MachineJobGetCurrentStatus(MachineInterface machine) {
        super(machine);
    }

    @Override
    public MachineStatus doJobWithResult() {
        return machine.getStatus();
    }

}
