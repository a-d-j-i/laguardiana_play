package machines.jobs;

import machines.MachineInterface;
import models.EnvelopeDeposit;

/**
 *
 * @author adji
 */
final public class MachineJobStartEnvelopeDepositAction extends MachineJob<Boolean> {

    final EnvelopeDeposit refDeposit;

    public MachineJobStartEnvelopeDepositAction(MachineInterface machine, EnvelopeDeposit refDeposit) {
        super(machine);
        this.refDeposit = refDeposit;
    }

    @Override
    public Boolean doJobWithResult() {
        return machine.onStartEnvelopeDeposit(refDeposit);
    }
}
