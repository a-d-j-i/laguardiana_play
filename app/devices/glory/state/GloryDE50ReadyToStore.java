package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.glory.GloryDE50Device.GloryDE50DeviceStateApi;
import devices.glory.GloryDE50Device.GloryDE50TaskType;
import devices.glory.state.poll.GloryDE50Store;
import devices.glory.state.poll.GloryDE50Withdraw;

/**
 *
 * @author adji
 */
public class GloryDE50ReadyToStore extends GloryDE50StateOperation {

    final GloryDE50StateAbstract prevStep;
    boolean sended = false;

    public GloryDE50ReadyToStore(GloryDE50DeviceStateApi api, GloryDE50StateAbstract prevStep) {
        super(api);
        this.prevStep = prevStep;
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract t) {
        DeviceTaskAbstract task = (DeviceTaskAbstract) t;
        switch ((GloryDE50TaskType) task.getType()) {
            case TASK_STORE_DEPOSIT:
                task.setReturnValue(true);
                return new GloryDE50Store(api);
            case TASK_WITHDRAW_DEPOSIT:
                task.setReturnValue(true);
                return new GloryDE50Withdraw(api);
        }
        return null;
    }
}
