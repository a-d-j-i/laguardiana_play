package machines.P500_GloryDE50.states;

import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskWithdraw;
import devices.glory.task.GloryDE50TaskCount;
import java.util.HashMap;
import machines.MachineDeviceDecorator;
import machines.P500_GloryDE50.MachineP500_GLORY;
import machines.states.MachineStateContextInterface;
import machines.states.MachineStateInterface;
import models.lov.Currency;
import play.Logger;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateContext implements MachineStateContextInterface {

    final public MachineP500_GLORY machine;
    final public MachineDeviceDecorator glory;
    final public Integer depositId;
    final public Integer currentUserId;

    public P500GloryDE50StateContext(MachineP500_GLORY machine, MachineDeviceDecorator glory, Integer depositId, Integer currentUserId) {
        this.machine = machine;
        this.glory = glory;
        this.depositId = depositId;
        this.currentUserId = currentUserId;
    }

    public boolean setCurrentState(MachineStateInterface prevState) {
        return machine.setCurrentState(prevState);
    }

}
