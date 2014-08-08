package machines.P500_MEI.states;

import devices.device.status.DeviceStatusInterface;
import devices.mei.status.MeiEbdsStatus;
import machines.MachineDeviceDecorator;
import models.db.LgDeposit.FinishCause;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500MeiStateAccepting extends P500MeiStateBillDepositContinue {

    public P500MeiStateAccepting(P500MEIStateContext context) {
        super(context);
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(MeiEbdsStatus.COUNTING)) {
            if (!context.cancel()) {
                Logger.error("Error calling machine.cancel");
            }
        } else if (st.is(MeiEbdsStatus.CANCELED)) {
            context.setCurrentState(new P500MeiStateBillDepositFinish(context, FinishCause.FINISH_CAUSE_OK));
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public String toString() {
        return "P500MeiStateAccepting{" + super.toString() + '}';
    }

}
