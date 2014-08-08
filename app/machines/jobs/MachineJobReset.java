package machines.jobs;

import machines.MachineInterface;

/**
 *
 * @author adji
 */
final public class MachineJobReset extends MachineJob<Boolean> {

    public MachineJobReset(MachineInterface machine) {
        super(machine);
    }

    @Override
    public Boolean doJobWithResult() {
        return machine.onReset();
    }

}
