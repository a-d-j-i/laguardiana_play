package machines.P500_MEI;

import devices.device.task.DeviceTaskCancel;
import devices.mei.MeiEbdsDevice;
import machines.MachineAbstract;
import machines.MachineDeviceDecorator;
import machines.P500_MEI.states.P500MeiStateBillDepositStart;
import machines.P500_MEI.states.P500MeiStateWaiting;
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
final public class MachineP500_MEI extends MachineAbstract {

    final private MachineDeviceDecorator mei;

    public MachineP500_MEI() {
        mei = new MachineDeviceDecorator("P500_MEI_DEVICE_MEI_EBDS", LgDevice.DeviceType.MEI_EBDS, new MeiEbdsDevice());
        addDevice(mei);
    }

    public boolean count(Currency currency) {
        return mei.count(currency);
    }

    public boolean cancel() {
        return mei.submitSynchronous(new DeviceTaskCancel());
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
