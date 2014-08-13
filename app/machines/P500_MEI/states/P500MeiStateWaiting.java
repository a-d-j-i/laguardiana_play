package machines.P500_MEI.states;

import devices.device.status.DeviceStatusError;
import devices.device.status.DeviceStatusInterface;
import machines.states.*;
import java.util.Date;
import machines.MachineDeviceDecorator;
import machines.status.MachineStatus;
import models.BillDeposit;
import play.Logger;

/**
 * Initial state that start things up.
 *
 * @author adji
 */
public class P500MeiStateWaiting extends MachineStateAbstract {

    private final P500MEIStateContext context;

    public P500MeiStateWaiting(P500MEIStateContext context) {
        this.context = context;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(DeviceStatusError.class)) {
            DeviceStatusError err = (DeviceStatusError) st;
            Logger.error("DEVICE ERROR : %s", err.getError());
            context.setCurrentState(new P500MeiStateError(this, context, err.getError()));
            return;
        }
        super.onDeviceEvent(dev, st);
    }

    @Override
    public boolean onStartBillDeposit(BillDeposit refBillDeposit) {
        Logger.debug("startBillDeposit start");
        BillDeposit d = new BillDeposit(refBillDeposit);
        d.startDate = new Date();
        d.save();
        context.setDeposit(d);
        return context.setCurrentState(new P500MeiStateBillDepositStart(context));
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatus(null, null, "WAITING");
    }

    @Override
    public String toString() {
        return "P500MeiStateWaiting";
    }

}
