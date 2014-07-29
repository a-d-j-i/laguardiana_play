package devices.glory.state;

import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.glory.GloryDE50Device.GloryDE50DeviceStateApi;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.task.GloryDE50TaskOperation;

/**
 *
 * @author adji
 */
abstract public class GloryDE50StateOperation extends GloryDE50StateAbstract {

    public GloryDE50StateOperation(GloryDE50DeviceStateApi api) {
        super(api);
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof GloryDE50TaskOperation) {
            GloryDE50TaskOperation op = (GloryDE50TaskOperation) task;
            GloryDE50OperationResponse response = new GloryDE50OperationResponse();
            String err = api.sendGloryDE50Operation(op.getOperation(), op.isDebug(), response);
            if (err == null) {
                op.setError(null);
                op.setResponse(response);
            } else {
                op.setError(err);
                op.setResponse(null);
            }
        }
        return command(task);
    }

    abstract public DeviceStateInterface command(DeviceTaskAbstract task);
}
