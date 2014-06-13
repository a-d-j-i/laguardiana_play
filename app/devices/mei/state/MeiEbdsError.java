package devices.mei.state;

import devices.device.status.DeviceStatusInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.MeiEbds;
import devices.mei.MeiEbdsDevice.MeiEbdsTaskType;
import devices.mei.state.MeiEbdsError.COUNTER_CLASS_ERROR_CODE;
import devices.mei.status.MeiEbdsStatusError;
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

    public MeiEbdsError(MeiEbds mei, COUNTER_CLASS_ERROR_CODE error_code, String error) {
        super(mei);
        this.error = error;
        Logger.error(error);
        mei.notifyListeners(new MeiEbdsStatusError(error));
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract t) {
        DeviceTaskAbstract task = (DeviceTaskAbstract) t;
        switch ((MeiEbdsTaskType) task.getType()) {
            case TASK_RESET:
                Logger.debug("executing reset task %s", task.toString());
                task.setReturnValue(true);
                return new MeiEbdsStateMain(mei);
            case TASK_OPEN_PORT:
                DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
                if (mei.open(open.getPort())) {
                    Logger.debug("MeiEbdsError new port %s", open.getPort());
                    task.setReturnValue(true);
                    return this;
                } else {
                    Logger.debug("MeiEbdsError new port %s failed to open", open.getPort());
                    task.setReturnValue(false);
                    return new MeiEbdsOpenPort(mei);
                }
        }
        return null;
    }

    public DeviceStatusInterface getStatus() {
        return new MeiEbdsStatusError(error);
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
