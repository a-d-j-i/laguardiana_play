package devices.mei.state;

import devices.device.DeviceStatusInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.MeiEbdsDevice.MeiEbdsDeviceStateApi;
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

    public MeiEbdsError(MeiEbdsDeviceStateApi api, COUNTER_CLASS_ERROR_CODE error_code, String error) {
        super(api);
        this.error = error;
        Logger.error(error);
        api.notifyListeners(new MeiEbdsStatusError(error));
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract t) {
        DeviceTaskAbstract task = (DeviceTaskAbstract) t;
        switch ((MeiEbdsTaskType) task.getType()) {
            case TASK_RESET:
                Logger.debug("executing reset task %s", task.toString());
                task.setReturnValue(true);
                return new MeiEbdsStateMain(api);
            case TASK_OPEN_PORT:
                DeviceTaskOpenPort open = (DeviceTaskOpenPort) task;
                task.setReturnValue(true);
                Logger.debug("executing open task %s", open.toString());
                return new MeiEbdsOpenPort(api, open.getPort());
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
     return new Reset(api, new GotoNeutral(api));
     }
     });
     }

     @Override
     public boolean storingErrorReset() {
     return comunicate(new Callable< MeiEbdsStateAbstract>() {
     public MeiEbdsStateAbstract call() throws Exception {
     return new StoringErrorReset(api);
     }
     });
     }

     @Override
     public boolean clearError() {
     return comunicate(new Callable< MeiEbdsStateAbstract>() {
     public MeiEbdsStateAbstract call() throws Exception {
     return new OpenPort(api);
     }
     });
     }

     @Override
     public String getError() {
     return error;
     }
     */
}
