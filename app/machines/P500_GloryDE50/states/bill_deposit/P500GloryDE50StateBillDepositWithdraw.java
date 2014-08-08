package machines.P500_GloryDE50.states.bill_deposit;

import machines.P500_GloryDE50.states.context.P500GloryDE50StateBillDepositContext;
import devices.device.status.DeviceStatusInterface;
import devices.glory.status.GloryDE50Status;
import machines.MachineDeviceDecorator;
import machines.P500_GloryDE50.states.P500GloryDE50StateError;
import machines.status.MachineBillDepositStatus;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateBillDepositWithdraw extends P500GloryDE50StateBillDepositContinue {

    public P500GloryDE50StateBillDepositWithdraw(P500GloryDE50StateBillDepositContext context) {
        super(context);
    }

    @Override
    public boolean onStart() {
        if (!context.withdraw()) {
            context.setCurrentState(new P500GloryDE50StateError(context, this, "Can't start P500GloryDE50StateBillDepositStart error in api.count"));
            return false;
        }
        return true;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {

        if (st.is(GloryDE50Status.GloryDE50StatusType.RETURNED)) {
            context.setCurrentState(new P500GloryDE50StateBillDepositStart(context));
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("REMOVE_THE_BILLS_FROM_ESCROW");
    }

    @Override
    public String toString() {
        return "P500GloryDE50StateBillDepositWithdraw{" + "context=" + context.toString() + '}';
    }
}
