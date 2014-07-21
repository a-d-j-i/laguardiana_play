package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.glory.GloryDE50Device.GloryDE50DeviceStateApi;
import java.util.Map;

/**
 * TODO: Use play jobs for this.
 *
 * @author adji
 */
abstract public class GloryDE50StateAbstract implements DeviceStateInterface {

    protected final GloryDE50DeviceStateApi api;

    public GloryDE50StateAbstract(GloryDE50DeviceStateApi api) {
        this.api = api;
    }

    public DeviceStateInterface init() {
        return this;
    }

    public boolean isError() {
        return false;
    }

    public boolean cancelDeposit() {
        return false;
    }

    public Integer getCurrency() {
        return null;
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        return null;
    }

    public Map<String, Integer> getDesiredQuantity() {
        return null;
    }

    public boolean clearError() {
        return false;
    }

    public String getError() {
        return null;
    }

    public GloryDE50StateAbstract getCollectState() {
        return null;
    }

    public DeviceStateInterface call(DeviceTaskAbstract task) {
        return null;
    }

}
