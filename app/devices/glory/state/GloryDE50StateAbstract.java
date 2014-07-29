package devices.glory.state;

import devices.device.state.DeviceStateAbstract;
import devices.device.state.DeviceStateInterface;
import devices.glory.GloryDE50Device;

/**
 * TODO: Use play jobs for this.
 *
 * @author adji
 */
abstract public class GloryDE50StateAbstract extends DeviceStateAbstract implements DeviceStateInterface {

    protected final GloryDE50Device api;

    public GloryDE50StateAbstract(GloryDE50Device api) {
        this.api = api;
    }

    @Override
    public String toString() {
        return "GloryDE50StateAbstract";
    }

    public boolean isError() {
        return false;
    }

}
