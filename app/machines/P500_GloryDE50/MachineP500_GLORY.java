package machines.P500_GloryDE50;

import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskWithdraw;
import devices.glory.GloryDE50Device;
import devices.glory.task.GloryDE50TaskCount;
import java.util.HashMap;
import machines.P500_GloryDE50.states.P500GloryDE50StateWaiting;
import machines.MachineAbstract;
import machines.MachineDeviceDecorator;
import machines.P500_GloryDE50.states.bill_deposit.P500GloryDE50StateBillDepositStart;
import machines.P500_GloryDE50.states.envelope_deposit.P500GloryDE50StateEnvelopeDepositStart;
import models.BillDeposit;
import models.EnvelopeDeposit;
import models.db.LgDeposit;
import models.db.LgDevice;
import models.lov.Currency;
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

    public boolean count(Currency currency) {
        /*        Map<String, Integer> slots = new HashMap<String, Integer>();
         for (LgDeviceSlot s : LgDeviceSlot.find(currency, glory.getLgDevice())) {
         slots.put(s.slot, null);
         }*/
        Integer c = 1;
        if (currency != null) {
            c = currency.numericId;
        }
        Logger.debug("Calling count on device %s, currency : %d", glory.toString(), c);
        return glory.submitSynchronous(new GloryDE50TaskCount(c, new HashMap<String, Integer>()));
    }

    public boolean cancel() {
        return glory.submitSynchronous(new DeviceTaskCancel());
    }

    public boolean withdraw() {
        return glory.submitSynchronous(new DeviceTaskWithdraw());
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
        setCurrentState(new P500GloryDE50StateWaiting(this));

        LgDeposit dep = LgDeposit.getCurrentDeposit();
        if (dep instanceof BillDeposit) {
            Logger.debug("--------> Start setting state to bill deposit %d", dep.depositId);
            setCurrentState(new P500GloryDE50StateBillDepositStart(this, dep.user.userId, dep.depositId));
        } else if (dep instanceof EnvelopeDeposit) {
            Logger.debug("--------> Start setting state to envelope deposit %d", dep.depositId);
            setCurrentState(new P500GloryDE50StateEnvelopeDepositStart(this, dep.user.userId, dep.depositId));
        }
        Logger.debug("Machine Start done");
    }
}
