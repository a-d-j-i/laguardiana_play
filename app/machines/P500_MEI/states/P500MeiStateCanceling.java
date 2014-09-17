package machines.P500_MEI.states;

import devices.device.status.DeviceStatusError;
import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskWithdraw;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatusReadyToStore;
import machines.MachineDeviceDecorator;
import machines.status.MachineBillDepositStatus;
import play.Logger;

/**
 * TODO: Move to MachineStateCanceling.
 *
 * @author adji
 */
public class P500MeiStateCanceling extends P500MeiStateAccepting {

    public P500MeiStateCanceling(P500MEIStateContext context) {
        super(context);
    }

    @Override
    public boolean onStart() {
        if (!context.cancel()) {
            Logger.error("Error calling machine.cancel");
            return false;
        }
        return true;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        Logger.debug("P500MeiStateCanceling DEVICE EVENT %s, %s", dev.toString(), st.toString());
        if (st.is(MeiEbdsStatus.NEUTRAL)) {
        } else if (st.is(MeiEbdsStatusReadyToStore.class)) {
            if (!dev.submitSynchronous(new DeviceTaskWithdraw())) {
                context.setCurrentState(new P500MeiStateError(this, context, "Error submitting withdraw"));
            }
            return;
        } else if (st.is(DeviceStatusError.class)) {
            DeviceStatusError err = (DeviceStatusError) st;
            context.setCurrentState(new P500MeiStateError(this, context, err.getError()));
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public MachineBillDepositStatus getStatus() {
        return getStatus("CANCELING");
    }

    @Override
    public String toString() {
        return "P500MeiStateCanceling{" + super.toString() + '}';
    }

}
