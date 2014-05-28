package devices.glory.state;

import devices.glory.GloryDE50Device.GloryDE50DeviceStateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50Error extends GloryDE50StateOperation {

    public enum COUNTER_CLASS_ERROR_CODE {

        GLORY_APPLICATION_ERROR, STORING_ERROR_CALL_ADMIN, BILLS_IN_ESCROW_CALL_ADMIN, CASSETE_FULL;
    }

    private final String error;

    public GloryDE50Error(GloryDE50DeviceStateApi api, COUNTER_CLASS_ERROR_CODE error_code, String error) {
        super(api);
        this.error = error;
        Logger.error(error);
        api.notifyListeners(error);
    }
    /*
     @Override
     public boolean reset() {
     return comunicate(new Callable< GloryDE50StateAbstract>() {
     public GloryDE50StateAbstract call() throws Exception {
     return new Reset(getApi(), new GotoNeutral(getApi()));
     }
     });
     }

     @Override
     public boolean storingErrorReset() {
     return comunicate(new Callable< GloryDE50StateAbstract>() {
     public GloryDE50StateAbstract call() throws Exception {
     return new StoringErrorReset(api);
     }
     });
     }

     @Override
     public boolean clearError() {
     return comunicate(new Callable< GloryDE50StateAbstract>() {
     public GloryDE50StateAbstract call() throws Exception {
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
