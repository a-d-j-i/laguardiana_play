package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.glory.GloryDE50DeviceStateApi;
import devices.glory.state.poll.GloryDE50Store;
import devices.glory.state.poll.GloryDE50Withdraw;
import devices.glory.task.GloryDE50TaskStoreDeposit;
import devices.glory.task.GloryDE50TaskWithdraw;

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

    public DeviceStateInterface call(GloryDE50TaskStoreDeposit task) {
        task.setReturnValue(true);
        return new GloryDE50Store(api);
    }

    public DeviceStateInterface call(GloryDE50TaskWithdraw task) {
        task.setReturnValue(true);
        return new GloryDE50Withdraw(api);
    }
}
