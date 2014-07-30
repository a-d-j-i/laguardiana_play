package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskStore;
import devices.device.task.DeviceTaskWithdraw;
import devices.glory.GloryDE50Device;
import devices.glory.state.poll.GloryDE50Store;
import devices.glory.state.poll.GloryDE50Withdraw;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50ReadyToStore extends GloryDE50StateOperation {

    final GloryDE50StateAbstract prevStep;
    boolean sended = false;

    public GloryDE50ReadyToStore(GloryDE50Device api, GloryDE50StateAbstract prevStep) {
        super(api);
        this.prevStep = prevStep;
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskStore) {
            task.setReturnValue(true);
            return new GloryDE50Store(api);
        } else if (task instanceof DeviceTaskWithdraw) {
            task.setReturnValue(true);
            return new GloryDE50Withdraw(api);
        } else {
            Logger.error("GloryDE50ReadyToStore Ignoring task : %s", task.toString());
        }
        return null;
    }
}
