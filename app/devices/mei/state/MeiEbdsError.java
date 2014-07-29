package devices.mei.state;

import devices.device.status.DeviceStatusInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.status.DeviceStatusError;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.device.task.DeviceTaskReset;
import devices.mei.MeiEbdsDevice;
import devices.mei.state.MeiEbdsError.COUNTER_CLASS_ERROR_CODE;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsError extends MeiEbdsStateAbstract {

    public enum COUNTER_CLASS_ERROR_CODE {

        MEI_EBDS_APPLICATION_ERROR, STORING_ERROR_CALL_ADMIN, BILLS_IN_ESCROW_CALL_ADMIN, CASSETE_FULL;
    }

    private final String error;

    public MeiEbdsError(MeiEbdsDevice mei, COUNTER_CLASS_ERROR_CODE error_code, String error) {
        super(mei);
        this.error = error;
        Logger.error(error);
        mei.notifyListeners(new DeviceStatusError(error));
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract t) {
        if (t instanceof DeviceTaskOpenPort) {
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
            Logger.debug("executing reset task %s", t.toString());
            t.setReturnValue(true);
            return new MeiEbdsStateMain(mei);
        }
        return null;
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
