package machines.jobs;

import machines.MachineInterface;
import models.BillDeposit;
import models.db.LgUser;
import models.lov.Currency;

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
