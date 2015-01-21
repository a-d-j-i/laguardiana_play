package machines.jobs;

import machines.MachineInterface;

/**
 *
 * @author adji
 */
final public class MachineJobStoringErrorReset extends MachineJob<Boolean> {

    public MachineJobStoringErrorReset(MachineInterface machine) {
        super(machine);
    }

    @Override
    public Boolean doJobWithResult() {
        return machine.onStoringErrorReset();
    }

}
