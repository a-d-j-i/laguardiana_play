package machines.P500_GloryDE50.states.context;

import devices.device.task.DeviceTaskCancel;
import devices.device.task.DeviceTaskReset;
import devices.device.task.DeviceTaskStoringErrorReset;
import devices.device.task.DeviceTaskWithdraw;
import machines.MachineDeviceDecorator;
import machines.P500_GloryDE50.MachineP500_GLORY;
import machines.states.MachineStateContextInterface;
import machines.states.MachineStateInterface;

/**
 *
 * @author adji
 */
public class P500GloryDE50StateContext implements MachineStateContextInterface {

    final MachineP500_GLORY machine;
    final MachineDeviceDecorator glory;

    public P500GloryDE50StateContext(MachineP500_GLORY machine, MachineDeviceDecorator glory) {
        this.machine = machine;
        this.glory = glory;
    }

    public boolean setCurrentState(MachineStateInterface prevState) {
        return machine.setCurrentState(prevState);
    }

    public boolean cancel() {
        return glory.submitSynchronous(new DeviceTaskCancel());
    }

    public boolean withdraw() {
        return glory.submitSynchronous(new DeviceTaskWithdraw());
    }

    public boolean reset() {
        return glory.submitSynchronous(new DeviceTaskReset());
    }

    public boolean storingErrorReset() {
        return glory.submitSynchronous(new DeviceTaskStoringErrorReset());
    }

    @Override
    public String toString() {
        return "P500GloryDE50StateContext{" + "machine=" + machine + ", glory=" + glory + '}';
    }

}
