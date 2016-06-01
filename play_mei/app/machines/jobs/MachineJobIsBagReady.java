package machines.jobs;

import machines.MachineInterface;

/**
 *
 * @author adji
 */
final public class MachineJobIsBagReady extends MachineJob<Boolean> {

    public MachineJobIsBagReady(MachineInterface machine) {
        super(machine);
    }

    @Override
    public Boolean doJobWithResult() {
        return machine.isBagReady();
    }

}
