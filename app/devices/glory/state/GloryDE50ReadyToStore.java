package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskStore;
import devices.device.task.DeviceTaskWithdraw;
import devices.glory.GloryDE50Device.GloryDE50DeviceStateApi;
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
        if (t instanceof DeviceTaskStore) {
            task.setReturnValue(true);
            return new GloryDE50Store(api);
        } else if (t instanceof DeviceTaskWithdraw) {
            task.setReturnValue(true);
            return new GloryDE50Withdraw(api);
        }
        return null;
    }
}
