package devices.ioboard.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.ioboard.IoboardDevice;
import play.Logger;

/**
 *
 * @author adji
 */
public class IoboardOpenPort extends IoboardStateAbstract {

    public IoboardOpenPort(IoboardDevice ioboard) {
        super(ioboard);
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract t) {
        Logger.debug("IoboardOpenPort got task %s", t.toString());
        if (t instanceof DeviceTaskOpenPort) {
            DeviceTaskOpenPort openPort = (DeviceTaskOpenPort) t;
            Logger.debug("calling ioboard open");
            if (ioboard.open(openPort.getPort())) {
                Logger.debug("IoboardOpenPort new port %s", openPort.getPort());
                t.setReturnValue(true);
                return new IoboardStateMain(ioboard);
            } else {
                Logger.debug("IoboardOpenPort new port %s failed to open", openPort.getPort());
            }
        } else {
            Logger.debug("IoboardOpenPort ignore task %s because need to open the port first", t.toString());
        }
        t.setReturnValue(false);
        return this;
    }

    @Override
    public String toString() {
        return "IoboardOpenPort";
    }

}
