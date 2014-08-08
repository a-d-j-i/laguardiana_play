package machines.P500_GloryDE50.states;

import machines.P500_GloryDE50.states.bill_deposit.P500GloryDE50StateBillDepositStart;
import machines.states.*;
import java.util.Date;
import machines.P500_GloryDE50.states.envelope_deposit.P500GloryDE50StateEnvelopeDepositStart;
import machines.status.MachineStatus;
import models.BillDeposit;
import models.EnvelopeDeposit;
import play.Logger;

/**
 * Initial state that start things up.
 *
 * @author adji
 */
public class P500GloryDE50StateWaiting extends MachineStateAbstract {

    public P500GloryDE50StateWaiting(MachineStateApiInterface machine) {
        super(machine);
    }

    @Override
    public boolean onStartBillDeposit(BillDeposit refDeposit) {
        BillDeposit d = new BillDeposit(refDeposit);
        d.startDate = new Date();
        d.save();
        return machine.setCurrentState(new P500GloryDE50StateBillDepositStart(machine, d.user.userId, d.depositId));
    }

    @Override
    public boolean onStartEnvelopeDeposit(EnvelopeDeposit refDeposit) {
        Logger.debug("startEnvelopeDeposit start");
        EnvelopeDeposit d = new EnvelopeDeposit(refDeposit);
        d.startDate = new Date();
        d.save();
        return machine.setCurrentState(new P500GloryDE50StateEnvelopeDepositStart(machine, d.user.userId, d.depositId));
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatus(null, null, "WAITING");
    }

    @Override
    public String toString() {
        return "P500GloryDE50StateWaiting";
    }

}
