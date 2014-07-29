package devices.glory;

import devices.device.DeviceAbstract;
import devices.device.DeviceMessageInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskOpenPort;
import devices.glory.state.GloryDE50OpenPort;
import java.util.Arrays;
import java.util.List;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50Device extends DeviceAbstract {

    private void debug(String message, Object... args) {
        // Logger.debug(message, args);
    }

    private final GloryDE50 glory = new GloryDE50(api);

    @Override
    protected DeviceMessageInterface getLastCommand() {
        return glory.getLastCommand();
    }

    @Override
    public void finish() {
        debug("GloryDE50 Executing finish");
        Logger.info("GloryDE50 Closing glory serial port ");
        glory.close();
    }

    @Override
    public List<String> getNeededProperties() {
        return Arrays.asList(new String[]{"port"});
    }

    @Override
    public boolean setProperty(String property, String value) {
        debug("trying to set property %s to %s", property, value);
        if (property.compareToIgnoreCase("port") == 0) {
            boolean ret = submitSynchronous(new DeviceTaskOpenPort(value));
            debug("changing port to %s %s", value, ret ? "SUCCESS" : "FAIL");
            return ret;
        }
        return false;
    }

    @Override
    public DeviceStateInterface initState() {
        return new GloryDE50OpenPort(glory);
    }

    @Override
    public String toString() {
        return glory.toString();
    }

}
