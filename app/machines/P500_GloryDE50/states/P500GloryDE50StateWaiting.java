package machines.P500_GloryDE50.states;

import machines.states.*;
import java.util.Date;
import machines.P500_GloryDE50.states.bill_deposit.GloryDE50BillDepositStart;
import machines.P500_GloryDE50.states.envelope_deposit.GloryDE50EnvelopeDepositStart;
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
        Logger.debug("startBillDeposit start");
        BillDeposit d = new BillDeposit(refDeposit);
        d.startDate = new Date();
        d.save();
        machine.setCurrentState(new GloryDE50BillDepositStart(machine, d.user.userId, d.depositId));
        if (!machine.count(d.currency)) {
            Logger.error("Can't start MachineActionBillDeposit error in api.count");
            return false;
        }
        return true;
    }

    @Override
    public boolean onStartEnvelopeDeposit(EnvelopeDeposit refDeposit) {
        Logger.debug("startEnvelopeDeposit start");
        EnvelopeDeposit d = new EnvelopeDeposit(refDeposit);
        d.startDate = new Date();
        d.save();
        machine.setCurrentState(new GloryDE50EnvelopeDepositStart(machine, d.user.userId, d.depositId));
        if (!machine.count(d.currency)) {
            Logger.error("Can't start MachineActionBillDeposit error in api.count");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "P500GloryDE50StateWaiting";
    }

}
