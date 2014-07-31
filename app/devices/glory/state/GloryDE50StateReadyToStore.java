package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskStore;
import devices.device.task.DeviceTaskWithdraw;
import devices.glory.GloryDE50Device;
import devices.glory.state.poll.GloryDE50StateStore;
import devices.glory.state.poll.GloryDE50StateWithdraw;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50StateReadyToStore extends GloryDE50StateAbstract {

    final GloryDE50StateAbstract prevStep;
    boolean sended = false;

    public GloryDE50StateReadyToStore(GloryDE50Device api, GloryDE50StateAbstract prevStep) {
        super(api);
        this.prevStep = prevStep;
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskStore) {
            task.setReturnValue(true);
            return new GloryDE50StateStore(api);
        } else if (task instanceof DeviceTaskWithdraw) {
            task.setReturnValue(true);
            return new GloryDE50StateWithdraw(api);
        } else {
            Logger.error("GloryDE50ReadyToStore Ignoring task : %s", task.toString());
        }
        return null;
    }
}
