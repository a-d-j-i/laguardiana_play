package machines.jobs;

import controllers.CountController;
import machines.MachineInterface;

/**
 *
 * @author adji
 */
final public class MachineJobStartCountingAction extends MachineJob<Boolean> {

    final CountController.CountData data;

    public MachineJobStartCountingAction(MachineInterface machine, CountController.CountData data) {
        super(machine);
        this.data = data;
    }

    @Override
    public Boolean doJobWithResult() {
        //return machine.currentState.startCountingAction(user, currency, userCode, userCodeLovId);
        return false;
    }

}
