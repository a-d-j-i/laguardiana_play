package machines.P500_GloryDE50.states;

import java.util.Date;
import machines.P500_GloryDE50.states.bill_deposit.P500GloryDE50StateBillDepositContext;
import machines.P500_GloryDE50.states.bill_deposit.P500GloryDE50StateBillDepositStart;
import machines.P500_GloryDE50.states.envelope_deposit.P500GloryDE50StateEnvelopeDepositStart;
import machines.states.MachineStateAbstract;
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

    final private P500GloryDE50StateContext context;

    public P500GloryDE50StateWaiting(P500GloryDE50StateContext context) {
        this.context = context;
    }

    @Override
    public boolean onStartBillDeposit(BillDeposit refDeposit) {
        BillDeposit d = new BillDeposit(refDeposit);
        d.startDate = new Date();
        d.save();
        return context.setCurrentState(new P500GloryDE50StateBillDepositStart(context));
    }

    @Override
    public boolean onStartEnvelopeDeposit(EnvelopeDeposit refDeposit) {
        Logger.debug("startEnvelopeDeposit start");
        EnvelopeDeposit d = new EnvelopeDeposit(refDeposit);
        d.startDate = new Date();
        d.save();
        return context.setCurrentState(new P500GloryDE50StateEnvelopeDepositStart(context));
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
