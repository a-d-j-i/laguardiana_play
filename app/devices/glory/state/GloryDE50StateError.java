package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.status.DeviceStatusError;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskReset;
import devices.device.task.DeviceTaskStoringErrorReset;
import devices.glory.GloryDE50Device;
import devices.glory.state.poll.GloryDE50StateReset;
import devices.glory.state.poll.GloryDE50StateStoringErrorReset;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50StateError extends GloryDE50StateAbstract {

    public enum COUNTER_CLASS_ERROR_CODE {

        GLORY_APPLICATION_ERROR, STORING_ERROR_CALL_ADMIN, BILLS_IN_ESCROW_CALL_ADMIN, CASSETE_FULL;
    }

    private final String error;

    public GloryDE50StateError(GloryDE50Device api, COUNTER_CLASS_ERROR_CODE error_code, String error) {
        super(api);
        this.error = error;
        Logger.error(error);
        api.notifyListeners(new DeviceStatusError(error));
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskReset) {
            task.setReturnValue(true);
            return new GloryDE50StateReset(api, new GloryDE50StateWaitForOperation(api));
        } else if (task instanceof DeviceTaskStoringErrorReset) {
            task.setReturnValue(true);
            return new GloryDE50StateStoringErrorReset(api);
        }
        return super.call(task);
    }

    @Override
    public String toString() {
        return "GloryDE50StateError{" + "error=" + error + '}';
    }
}
