package machines.P500_GloryDE50.states.bill_deposit;

import devices.device.status.DeviceStatusInterface;
import devices.glory.status.GloryDE50Status;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateApiInterface;
import machines.status.MachineBillDepositStatus;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateBillDepositWithdraw extends P500GloryDE50StateBillDepositContinue {

    public P500GloryDE50StateBillDepositWithdraw(MachineStateApiInterface machine, P500GloryDE50StateBillDepositInfo info) {
        super(machine, info);
    }

    @Override
    public boolean onStart() {
        if (!machine.withdraw()) {
            Logger.error("Can't start P500GloryDE50StateBillDepositStart error in api.count");
            return false;
        }
        return true;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(GloryDE50Status.GloryDE50StatusType.RETURNED)) {
            machine.setCurrentState(new P500GloryDE50StateBillDepositContinue(machine, info));
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("REMOVE_THE_BILLS_FROM_ESCROW");
    }
}
