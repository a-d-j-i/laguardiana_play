package machines.P500_MEI;

import devices.ioboard.IoboardDevice;
import devices.mei.MeiEbdsDevice;
import machines.MachineDeviceDecorator;
import machines.MachineWithBagAbstract;
import machines.P500_MEI.states.P500MEIStateContext;
import machines.P500_MEI.states.P500MeiStateBillDepositStart;
import machines.P500_MEI.states.P500MeiStateEnvelopeDepositMain;
import machines.P500_MEI.states.P500MeiStateWaiting;
import models.BillDeposit;
import models.EnvelopeDeposit;
import models.db.LgDeposit;
import models.db.LgDevice;
import play.Logger;

/**
 *
 * @author adji
 */
final public class MachineP500_MEI extends MachineWithBagAbstract {

    final private MachineDeviceDecorator mei;

    public MachineP500_MEI() {
        // ioboard
        super(new MachineDeviceDecorator("P500_MEI_DEVICE_IOBOARD", LgDevice.DeviceType.IO_BOARD_MEI_1_0,
                new IoboardDevice(IoboardDevice.IoBoardDeviceType.IOBOARD_DEVICE_TYPE_MEI_1_0)));
        // counter.
        mei = new MachineDeviceDecorator("P500_MEI_DEVICE_MEI_EBDS", LgDevice.DeviceType.MEI_EBDS,
                new MeiEbdsDevice());
        addDevice(mei);
    }

    @Override
    public void start() {
        super.start();

        P500MEIStateContext context = new P500MEIStateContext(this, mei);

        // set current action.
        LgDeposit dep = LgDeposit.getCurrentDeposit();
        context.setDeposit(dep);
        setCurrentState(new P500MeiStateWaiting(context));
        if (dep instanceof BillDeposit) {
            Logger.debug("--------> Start setting state to bill deposit %d", dep.depositId);
            setCurrentState(new P500MeiStateBillDepositStart(context));
        } else if (dep instanceof EnvelopeDeposit) {
            Logger.debug("--------> Start setting state to envelope deposit %d", dep.depositId);
            setCurrentState(new P500MeiStateEnvelopeDepositMain(context));
//            Logger.error("THIS MACHINE DON'T SUPPORT ENVELOPE DEPOSIT, CANCELING DEPOSIT");
        }
        Logger.debug("Machine Start done");
    }

}
