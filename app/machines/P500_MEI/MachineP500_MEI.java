package machines.P500_MEI;

import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskWithdraw;
import devices.mei.MeiEbdsDevice;
import devices.mei.task.MeiEbdsTaskCount;
import java.util.HashMap;
import java.util.Map;
import machines.MachineAbstract;
import machines.MachineDeviceDecorator;
import machines.P500_MEI.states.P500MeiStateBillDepositStart;
import machines.P500_MEI.states.P500MeiStateWaiting;
import models.BillDeposit;
import models.EnvelopeDeposit;
import models.db.LgDeposit;
import models.db.LgDevice;
import models.db.LgDeviceSlot;
import models.lov.Currency;
import play.Logger;

/**
 *
 * @author adji
 */
final public class MachineP500_MEI extends MachineAbstract {

    final private MachineDeviceDecorator mei;

    public MachineP500_MEI() {
        mei = new MachineDeviceDecorator("P500_MEI_DEVICE_MEI_EBDS", LgDevice.DeviceType.MEI_EBDS, new MeiEbdsDevice());
        addDevice(mei);
    }

    public boolean count(Currency currency) {
        Map<String, Integer> slots = new HashMap<String, Integer>();
        for (LgDeviceSlot s : LgDeviceSlot.find(currency, mei.getLgDevice())) {
            slots.put(s.slot, null);
        }
        Logger.debug("Calling count on device %s, slots : %s", mei.toString(), slots.toString());
        return mei.submitSynchronous(new MeiEbdsTaskCount(slots));
    }

    public boolean cancel() {
        return mei.submitSynchronous(new DeviceTaskCancel());
    }

    public boolean withdraw() {
        return mei.submitSynchronous(new DeviceTaskWithdraw());
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
        // set current action.
        setCurrentState(new P500MeiStateWaiting(this));

        LgDeposit dep = LgDeposit.getCurrentDeposit();
        if (dep instanceof BillDeposit) {
            Logger.debug("--------> Start setting state to bill deposit %d", dep.depositId);
            setCurrentState(new P500MeiStateBillDepositStart(this, dep.user.userId, dep.depositId));
        } else if (dep instanceof EnvelopeDeposit) {
//            Logger.debug("--------> Start setting state to envelope deposit %d", dep.depositId);
//            currentState = new P500MeiStateEnvelopeMain(this, (EnvelopeDeposit) dep);
            Logger.error("THIS MACHINE DON'T SUPPORT ENVELOPE DEPOSIT, CANCELING DEPOSIT");
        }
        Logger.debug("Machine Start done");
    }

}
