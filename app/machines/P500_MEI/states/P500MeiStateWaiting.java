package machines.P500_MEI.states;

import machines.states.*;
import java.util.Date;
import models.BillDeposit;
import play.Logger;

/**
 * Initial state that start things up.
 *
 * @author adji
 */
public class P500MeiStateWaiting extends MachineStateAbstract {

    public P500MeiStateWaiting(MachineStateApiInterface machine) {
        super(machine);
    }

    @Override
    public boolean onStartBillDeposit(BillDeposit refBillDeposit) {
        Logger.debug("startBillDeposit start");
        BillDeposit d = new BillDeposit(refBillDeposit);
        d.startDate = new Date();
        d.save();
        machine.setCurrentState(new P500MeiStateBillDepositStart(machine, d.user.userId, d.depositId));
        if (!machine.count(refBillDeposit.currency)) {
            Logger.error("Can't start MachineActionBillDeposit error in api.count");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "P500MeiStateWaiting";
    }

}
