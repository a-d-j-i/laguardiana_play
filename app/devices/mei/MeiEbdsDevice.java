package devices.mei;

import devices.device.DeviceAbstract;
import devices.device.DeviceMessageInterface;
import devices.device.state.DeviceStateInterface;
import devices.mei.state.MeiEbdsOpenPort;
import devices.device.task.DeviceTaskOpenPort;
import java.util.Arrays;
import java.util.List;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsDevice extends DeviceAbstract {

    private void debug(String message, Object... args) {
        // Logger.debug(message, args);
    }

    private final MeiEbds mei = new MeiEbds(api);

    @Override
    protected DeviceMessageInterface getLastCommand() {
        return mei.getLastCommand();
    }

    @Override
    public void finish() {
        debug("MeiEbds Executing finish");
        Logger.info("MeiEbds Closing mei serial port ");
        mei.close();
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
        return new MeiEbdsOpenPort(mei);
    }

    @Override
    public String toString() {
        return mei.toString();
    }

}
