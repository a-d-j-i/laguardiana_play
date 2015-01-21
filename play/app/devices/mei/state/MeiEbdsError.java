package devices.mei.state;

import devices.device.status.DeviceStatusInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.status.DeviceStatusError;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.device.task.DeviceTaskReset;
import devices.mei.MeiEbdsDevice;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsError extends MeiEbdsStateAbstract {

    private final String error;

    public MeiEbdsError(MeiEbdsDevice mei, String error) {
        super(mei);
        this.error = error;
        Logger.error(error);
        mei.notifyListeners(new DeviceStatusError(error));
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract t) {
        if (t instanceof DeviceTaskOpenPort) {
            Logger.debug("MeiEbdsError task : %s", t.toString());
            DeviceTaskOpenPort open = (DeviceTaskOpenPort) t;
            if (mei.open(open.getPort())) {
                Logger.debug("MeiEbdsError new port %s", open.getPort());
                open.setReturnValue(true);
                return this;
            } else {
                Logger.debug("MeiEbdsError new port %s failed to open", open.getPort());
                open.setReturnValue(false);
                return new MeiEbdsOpenPort(mei);
            }
        } else if (t instanceof DeviceTaskReset) {
            Logger.debug("MeiEbdsError executing reset task %s", t.toString());
            t.setReturnValue(true);
            return new MeiEbdsStateMain(mei);
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
     return comunicate(new Callable< MeiEbdsStateAbstract>() {
     public MeiEbdsStateAbstract call() throws Exception {
     return new Reset(mei, new GotoNeutral(mei));
     }
     });
     }

     @Override
     public boolean storingErrorReset() {
     return comunicate(new Callable< MeiEbdsStateAbstract>() {
     public MeiEbdsStateAbstract call() throws Exception {
     return new StoringErrorReset(mei);
     }
     });
     }

     @Override
     public boolean clearError() {
     return comunicate(new Callable< MeiEbdsStateAbstract>() {
     public MeiEbdsStateAbstract call() throws Exception {
     return new OpenPort(mei);
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
        return "MeiEbdsError{" + "error=" + error + '}';
    }
}
