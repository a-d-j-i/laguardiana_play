package devices.mei.state;

import devices.device.state.DeviceStateInterface;
import devices.glory.state.poll.GloryDE50GotoNeutral;
import devices.mei.MeiEbdsDeviceStateApi;
import devices.mei.state.MeiEbdsError.COUNTER_CLASS_ERROR_CODE;
import devices.mei.task.MeiEbdsTaskReset;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsError extends MeiEbdsStateOperation {

    public enum COUNTER_CLASS_ERROR_CODE {

        MEI_EBDS_APPLICATION_ERROR, STORING_ERROR_CALL_ADMIN, BILLS_IN_ESCROW_CALL_ADMIN, CASSETE_FULL;
    }

    private final String error;

    public MeiEbdsError(MeiEbdsDeviceStateApi api, COUNTER_CLASS_ERROR_CODE error_code, String error) {
        super(api);
        this.error = error;
        Logger.error(error);
        api.notifyListeners(error);
    }

    @Override
    boolean isError() {
        return false;
    }

    public DeviceStateInterface call(MeiEbdsTaskReset task) {
        task.setReturnValue(true);
        return new MeiEbdsReset(api, this);
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
