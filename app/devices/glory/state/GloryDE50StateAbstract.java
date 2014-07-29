package devices.glory.state;

import devices.device.state.DeviceStateAbstract;
import devices.device.state.DeviceStateInterface;
import devices.glory.GloryDE50Device.GloryDE50DeviceStateApi;

/**
 * TODO: Use play jobs for this.
 *
 * @author adji
 */
abstract public class GloryDE50StateAbstract extends DeviceStateAbstract implements DeviceStateInterface {

    protected final GloryDE50DeviceStateApi api;

    public GloryDE50StateAbstract(GloryDE50DeviceStateApi api) {
        this.api = api;
    }

    @Override
    public String toString() {
        return "GloryDE50StateAbstract";
    }
}
