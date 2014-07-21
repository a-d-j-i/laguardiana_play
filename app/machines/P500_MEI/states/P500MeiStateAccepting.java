package machines.P500_MEI.states;

import devices.device.status.DeviceStatusInterface;
import devices.mei.status.MeiEbdsStatus;
import machines.MachineDeviceDecorator;
import machines.states.MachineStateApiInterface;
import models.db.LgDeposit.FinishCause;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateAccepting extends P500MeiStateBillDepositContinue {

    public P500MeiStateAccepting(MachineStateApiInterface machine, Integer currentUserId, Integer billDepositId, Integer batchId) {
        super(machine, currentUserId, billDepositId, batchId);
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(MeiEbdsStatus.COUNTING)) {
            if (!machine.cancel()) {
                Logger.error("Error calling machine.cancel");
            }
        } else if (st.is(MeiEbdsStatus.CANCELED)) {
            machine.setCurrentState(new P500MeiStateFinish(machine, currentUserId, billDepositId, FinishCause.FINISH_CAUSE_OK));
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public String toString() {
        return "P500MeiStateAccepting{" + super.toString() + '}';
    }

}
