package machines.P500_MEI.states;

import machines.states.*;
import java.util.Date;
import models.BillDeposit;
import models.db.LgUser;
import models.lov.Currency;
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
    public boolean onStartBillDeposit(LgUser user, Currency currency, String userCode, Integer userCodeLovId) {
        Logger.debug("startBillDeposit start");
        BillDeposit d = new BillDeposit(user, currency, userCode, userCodeLovId);
        d.startDate = new Date();
        d.save();
        machine.setCurrentState(new P500MeiStateBillDepositStart(machine, d.user.userId, d.depositId));
        if (!machine.count(currency)) {
            Logger.error("Can't start MachineActionBillDeposit error in api.count");
            return false;
        }
        return true;
    }

//    @Override
//    public boolean onStartEnvelopeDeposit(LgUser user, String userCode, Integer userCodeLovId) {
//        Logger.debug("MachineJobStartAction doJobWithResult start");
//        EnvelopeDeposit d = new EnvelopeDeposit(user, userCode, userCodeLovId);
//        d.startDate = new Date();
//        d.save();
//        return new P500MeiStateBillDepositMain(machine, d);
//    }
    @Override
    public String toString() {
        return "P500MeiStateWaiting";
    }

}
