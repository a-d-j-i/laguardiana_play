package machines.jobs;

import machines.MachineInterface;

/**
 *
 * @author adji
 */
final public class MachineJobIsBagFull extends MachineJob<Boolean> {

    public MachineJobIsBagFull(MachineInterface machine) {
        super(machine);
    }

    @Override
    public Boolean doJobWithResult() {
        return machine.isBagFull();
    }

}
