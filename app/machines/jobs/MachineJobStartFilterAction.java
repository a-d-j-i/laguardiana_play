package machines.jobs;

import controllers.FilterController;
import machines.MachineInterface;

/**
 *
 * @author adji
 */
final public class MachineJobStartFilterAction extends MachineJob<Boolean> {

    final FilterController.FilterData data;

    public MachineJobStartFilterAction(MachineInterface machine, FilterController.FilterData data) {
        super(machine);
        this.data = data;
    }

    @Override
    public Boolean doJobWithResult() {
//        return new MachineStateInfoFilter((MachineActionApiInterface) machine, data);
        return false;
    }

}
