package machines.jobs;

import machines.MachineInterface;
import models.db.LgUser;
import models.lov.Currency;

/**
 *
 * @author adji
 */
final public class MachineJobStartBillDepositAction extends MachineJob<Boolean> {

    final LgUser user;
    final Currency currency;
    final String userCode;
    final Integer userCodeLovId;

    public MachineJobStartBillDepositAction(MachineInterface machine, LgUser user, Currency currency, String userCode, Integer userCodeLovId) {
        super(machine);
        this.user = user;
        this.currency = currency;
        this.userCode = userCode;
        this.userCodeLovId = userCodeLovId;
    }

    @Override
    public Boolean doJobWithResult() {
        return machine.onStartBillDeposit(user, currency, userCode, userCodeLovId);
    }
}
