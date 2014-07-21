package machines.jobs;

import machines.MachineInterface;
import models.db.LgUser;

/**
 *
 * @author adji
 */
final public class MachineJobStartEnvelopeDepositAction extends MachineJob<Boolean> {

    final LgUser user;
    final String userCode;
    final Integer userCodeLovId;

    public MachineJobStartEnvelopeDepositAction(MachineInterface machine, LgUser user, String userCode, Integer userCodeLovId) {
        super(machine);
        this.user = user;
        this.userCode = userCode;
        this.userCodeLovId = userCodeLovId;
    }

    @Override
    public Boolean doJobWithResult() {
//        EnvelopeDeposit d = new EnvelopeDeposit(user, userCode, userCodeLovId);
//        d.startDate = new Date();
//        d.save();
//        return new MachineStateInfoEnvelopeDeposit((MachineActionApiInterface) machine, d);
        return machine.onStartEnvelopeDeposit(user, userCode, userCodeLovId);
    }

}
