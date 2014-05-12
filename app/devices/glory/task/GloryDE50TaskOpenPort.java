package devices.glory.task;

import devices.device.state.DeviceStateAbstract;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.state.OpenPort;

/**
 *
 * @author adji
 */
public class GloryDE50TaskOpenPort extends GloryDE50TaskAbstract<Boolean> {

    private final String port;

    public GloryDE50TaskOpenPort(String port) {
        this.port = port;
    }

    @Override
    protected DeviceStateAbstract call(GloryDE50StateAbstract currentState) {
        boolean ret = currentState.acceptOpenPort();
        setReturnValue(ret);
        if (ret) {
            return new OpenPort(currentState.getApi(), port);
        }
        return null;
    }

}
