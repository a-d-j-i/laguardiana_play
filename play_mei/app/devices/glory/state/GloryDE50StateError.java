package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.status.DeviceStatusError;
import devices.device.status.DeviceStatusStoringError;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskReset;
import devices.device.task.DeviceTaskStoringErrorReset;
import devices.glory.GloryDE50Device;
import devices.glory.state.poll.GloryDE50StateReset;
import devices.glory.state.poll.GloryDE50StateStoringErrorReset;
import devices.glory.task.GloryDE50TaskOperation;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50StateError extends GloryDE50StateAbstract {

    public enum COUNTER_CLASS_ERROR_CODE {

        GLORY_APPLICATION_ERROR() {
                    @Override
                    void notifyError(GloryDE50Device api, String error) {
                        Logger.error(error);
                        api.notifyListeners(new DeviceStatusError(error));
                    }
                }, STORING_ERROR_CALL_ADMIN {
                    @Override
                    void notifyError(GloryDE50Device api, String error) {
                        Logger.error(error);
                        api.notifyListeners(new DeviceStatusStoringError(error));
                    }
                }, BILLS_IN_ESCROW_CALL_ADMIN {
                    @Override
                    void notifyError(GloryDE50Device api, String error) {
                        Logger.error(error);
                        api.notifyListeners(new DeviceStatusStoringError(error));
                    }
                }, CASSETE_FULL {
                    @Override
                    void notifyError(GloryDE50Device api, String error) {
                        Logger.error(error);
                        api.notifyListeners(new DeviceStatusError(error));
                    }
                };

        abstract void notifyError(GloryDE50Device api, String error);
    }

    private final String error;
    private final COUNTER_CLASS_ERROR_CODE error_code;

    public GloryDE50StateError(GloryDE50Device api, COUNTER_CLASS_ERROR_CODE error_code, String error) {
        super(api);
        this.error = error;
        this.error_code = error_code;
        error_code.notifyError(api, error);
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskReset) {
            task.setReturnValue(true);
            return new GloryDE50StateReset(api, new GloryDE50StateWaitForOperation(api));
        } else if (task instanceof DeviceTaskStoringErrorReset) {
            task.setReturnValue(true);
            return new GloryDE50StateStoringErrorReset(api);
        } else if (task instanceof GloryDE50TaskOperation) {
            GloryDE50TaskOperation opt = (GloryDE50TaskOperation) task;
            String err = api.writeOperation(opt, true);
            if (err != null) {
                opt.setError(err);
                task.setReturnValue(false);
                return null;
            }
            return new GloryDE50StateWaitForResponse(api, opt, this);
        } else { // TODO: allways ?
            error_code.notifyError(api, error);
        }
        return super.call(task);
    }

    @Override
    public String toString() {
        return "GloryDE50StateError{" + "error=" + error + '}';
    }
}
