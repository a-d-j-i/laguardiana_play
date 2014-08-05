package machines.jobs;

import machines.MachineInterface;
import models.BillDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
final public class MachineJobStartBillDepositAction extends MachineJob<Boolean> {

    final BillDeposit refDeposit;

    public MachineJobStartBillDepositAction(MachineInterface machine, BillDeposit refDeposit) {
        super(machine);
        this.refDeposit = refDeposit;
    }

    @Override
    public Boolean doJobWithResult() {
        return machine.onStartBillDeposit(refDeposit);
    }
}
