package machines.P500_GloryDE50;

import devices.device.task.DeviceTaskCancel;
import devices.glory.GloryDE50Device;
import machines.P500_GloryDE50.states.P500GloryDE50StateWaiting;
import machines.MachineAbstract;
import machines.MachineDeviceDecorator;
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
        return glory.count(currency);
    }

    public boolean cancel() {
        return glory.submitSynchronous(new DeviceTaskCancel());
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
//
//        LgDeposit dep = LgDeposit.getCurrentDeposit();
//        if (dep instanceof BillDeposit) {
//            Logger.debug("--------> Start setting state to bill deposit %d", dep.depositId);
//            setCurrentState(new GloryDE50BillDepositStart(this, dep.user.userId, dep.depositId));
//        } else if (dep instanceof EnvelopeDeposit) {
//            Logger.debug("--------> Start setting state to envelope deposit %d", dep.depositId);
//            setCurrentState(new GloryDE50EnvelopeDepositStart(this, dep.user.userId, dep.depositId));
//        }
        Logger.debug("Machine Start done");
    }

    /*
     @Override
     public void start() {
     super.start();
     // set current action.
     setCurrentState(new GloryDE50StateWaiting(this));

     LgDeposit dep = LgDeposit.getCurrentDeposit();
     if (dep instanceof BillDeposit) {
     Logger.debug("--------> Start setting state to bill deposit %d", dep.depositId);
     setCurrentState(new GloryDE50BillDepositStart(this, dep.user.userId, dep.depositId));
     } else if (dep instanceof EnvelopeDeposit) {
     Logger.debug("--------> Start setting state to envelope deposit %d", dep.depositId);
     setCurrentState(new GloryDE50EnvelopeDepositStart(this, dep.user.userId, dep.depositId));
     }
     Logger.debug("Machine Start done");
     }
     */
}
