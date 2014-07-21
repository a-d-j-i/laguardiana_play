package machines.states;

import devices.device.status.DeviceStatusInterface;
import devices.mei.status.MeiEbdsStatus;
import machines.MachineAbstract;
import machines.MachineDeviceDecorator;
import machines.status.MachineStatus;

/**
 *
 * @author adji
 */
public class MachineStateJam extends MachineStateAbstract {

    final protected MachineStateAbstract prevState;
    final protected String action;
    final protected Integer currentUserId;

    public MachineStateJam(MachineStateApiInterface machine, MachineStateAbstract prevState, Integer userId, String action) {
        super(machine);
        this.prevState = prevState;
        this.action = action;
        this.currentUserId = userId;
    }

    @Override
    public void onDeviceEvent(MachineDeviceDecorator dev, DeviceStatusInterface st) {
        if (st.is(MeiEbdsStatus.NEUTRAL)) {
            machine.setCurrentState(prevState);
        } else {
            // TODO: Ignore ?
            prevState.onDeviceEvent(dev, st);
        }
    }

    @Override
    public MachineStatus getStatus() {
        return new MachineStatus(currentUserId, action, "JAM", "message.jam");
    }

}
