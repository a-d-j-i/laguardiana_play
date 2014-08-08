package machines.P500_GloryDE50;

import devices.glory.GloryDE50Device;
import machines.P500_GloryDE50.states.P500GloryDE50StateWaiting;
import machines.MachineAbstract;
import machines.MachineDeviceDecorator;
import machines.P500_GloryDE50.states.context.P500GloryDE50StateContext;
import machines.P500_GloryDE50.states.bill_deposit.P500GloryDE50StateBillDepositStart;
import machines.P500_GloryDE50.states.context.P500GloryDE50StateBillDepositContext;
import machines.P500_GloryDE50.states.context.P500GloryDE50StateEnvelopeDepositContext;
import machines.P500_GloryDE50.states.envelope_deposit.P500GloryDE50StateEnvelopeDepositStart;
import models.BillDeposit;
import models.EnvelopeDeposit;
import models.db.LgDeposit;
import models.db.LgDevice;
import play.Logger;

/**
 *
 * @author adji
 */
final public class MachineP500_GLORY extends MachineAbstract {

    final private MachineDeviceDecorator glory;

    public MachineP500_GLORY() {
        glory = new MachineDeviceDecorator("P500_GLORY_DEVICE_GLORY_DE50", LgDevice.DeviceType.GLORY_DE50, new GloryDE50Device());
        addDevice(glory);
    }

    public boolean isBagFull() {
        return true;
    }

    public boolean isBagReady() {
        return true;
    }

    @Override
    public void start() {
        super.start();

        P500GloryDE50StateContext context = new P500GloryDE50StateContext(this, glory);

        // set current action.
        LgDeposit dep = LgDeposit.getCurrentDeposit();
        setCurrentState(new P500GloryDE50StateWaiting(context));

        if (dep instanceof BillDeposit) {
            Logger.debug("--------> Start setting state to bill deposit %d", dep.depositId);
            setCurrentState(new P500GloryDE50StateBillDepositStart(new P500GloryDE50StateBillDepositContext(context, (BillDeposit) dep)));
        } else if (dep instanceof EnvelopeDeposit) {
            Logger.debug("--------> Start setting state to envelope deposit %d", dep.depositId);
            setCurrentState(new P500GloryDE50StateEnvelopeDepositStart(new P500GloryDE50StateEnvelopeDepositContext(context, (EnvelopeDeposit) dep)));
        }
        Logger.debug("Machine Start done");
    }
}
