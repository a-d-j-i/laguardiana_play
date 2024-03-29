package machines.P500_MEI.states;

import devices.device.status.DeviceStatusError;
import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskWithdraw;
import devices.ioboard.status.IoBoardStatusError;
import devices.mei.status.MeiEbdsStatus;
import devices.mei.status.MeiEbdsStatusReadyToStore;
import machines.MachineDeviceDecorator;
import machines.status.MachineBillDepositStatus;
import models.Configuration;
import models.db.LgDeposit;
import play.Logger;

/**
 * TODO: Move to MachineStateCanceling.
 *
 * @author adji
 */
public class P500MeiStateCanceling extends P500MeiStateBillDepositContinue {

    private final LgDeposit.FinishCause finishCause;

    P500MeiStateCanceling(P500MEIStateContext context, LgDeposit.FinishCause finishCause) {
        super(context);
        this.finishCause = finishCause;
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
            if (st.is(IoBoardStatusError.class)) {
                if (Configuration.isIgnoreIoBoard()) {
                    return;
                }
            }
            DeviceStatusError err = (DeviceStatusError) st;
            context.setCurrentState(new P500MeiStateError(this, context, err.getError()));
            return;
        } else if (st.is(MeiEbdsStatus.CANCELED)) {
            context.setCurrentState(new P500MeiStateBillDepositFinish(context, finishCause));
            return;
        } else if (st.is(MeiEbdsStatus.COUNTING)) {
            if (!context.cancel()) {
                Logger.error("Error calling machine.cancel");
            }
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
