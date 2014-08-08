package machines.P500_GloryDE50.states.bill_deposit;

import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskWithdraw;
import devices.glory.status.GloryDE50Status;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateError;
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
        if (!context.glory.submitSynchronous(new DeviceTaskWithdraw())) {
            context.setCurrentState(new MachineStateError(this, context.currentUserId, "Can't start P500GloryDE50StateBillDepositStart error in api.count"));
            return false;
        }
        return true;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {

        if (st.is(GloryDE50Status.GloryDE50StatusType.RETURNED)) {
            context.setCurrentState(new P500GloryDE50StateBillDepositContinue(context));
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("REMOVE_THE_BILLS_FROM_ESCROW");
    }
}
