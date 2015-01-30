package devices.ioboard.state;

import devices.device.status.DeviceStatusInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.status.DeviceStatusError;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.device.task.DeviceTaskReset;
import devices.ioboard.IoboardDevice;
import play.Logger;

/**
 *
 * @author adji
 */
public class IoboardError extends IoboardStateAbstract {

    private final String error;

    public IoboardError(IoboardDevice ioboard, String error) {
        super(ioboard);
        this.error = error;
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract t) {
        Logger.debug("IoboardError task : %s", t.toString());
        if (t instanceof DeviceTaskOpenPort) {
            DeviceTaskOpenPort open = (DeviceTaskOpenPort) t;
            if (ioboard.open(open.getPort())) {
                Logger.debug("IoboardError new port %s", open.getPort());
                open.setReturnValue(true);
                return this;
            } else {
                Logger.debug("IoboardError new port %s failed to open", open.getPort());
                open.setReturnValue(false);
                return new IoboardOpenPort(ioboard);
            }
        } else if (t instanceof DeviceTaskReset) {
            Logger.debug("executing reset task %s", t.toString());
            t.setReturnValue(true);
            return new IoboardStateMain(ioboard);
        } else {
            Logger.error(error);
            ioboard.notifyListeners(new DeviceStatusError(error));
        }
        t.setReturnValue(false);
        return this;
    }

    public DeviceStatusInterface getStatus() {
        return new DeviceStatusError(error);
    }

    /*
     @Override
     public boolean reset() {
     return comunicate(new Callable< IoboardStateAbstract>() {
     public IoboardStateAbstract call() throws Exception {
     return new Reset(ioboard, new GotoNeutral(ioboard));
     }
     });
     }

     @Override
     public boolean storingErrorReset() {
     return comunicate(new Callable< IoboardStateAbstract>() {
     public IoboardStateAbstract call() throws Exception {
     return new StoringErrorReset(ioboard);
     }
     });
     }

     @Override
     public boolean clearError() {
     return comunicate(new Callable< IoboardStateAbstract>() {
     public IoboardStateAbstract call() throws Exception {
     return new OpenPort(ioboard);
     }
     });
     }

     @Override
     public String getError() {
     return error;
     }
     */
    @Override
    public String toString() {
        return "IoboardError{" + "error=" + error + '}';
    }
}
