package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.MeiEbds;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsOpenPort extends MeiEbdsStateAbstract {

    public MeiEbdsOpenPort(MeiEbds mei) {
        super(mei);
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract t) {
        Logger.debug("MeiEbdsOpenPort got task %s", t.toString());
        if (t instanceof DeviceTaskOpenPort) {
            DeviceTaskOpenPort openPort = (DeviceTaskOpenPort) t;
            Logger.debug("calling mei open");
            if (mei.open(openPort.getPort())) {
                Logger.debug("MeiEbdsOpenPort new port %s", openPort.getPort());
                t.setReturnValue(true);
                return new MeiEbdsStateMain(mei);
            } else {
                Logger.debug("MeiEbdsOpenPort new port %s failed to open", openPort.getPort());
            }
        } else {
            Logger.debug("MeiEbdsOpenPort ignore task %s because need to open the port first", t.toString());
        }
        t.setReturnValue(false);
        return this;
    }

    @Override
    public String toString() {
        return "MeiEbdsOpenPort";
    }

}
