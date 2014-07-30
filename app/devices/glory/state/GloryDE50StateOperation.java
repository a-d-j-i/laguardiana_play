package devices.glory.state;

import devices.device.DeviceResponseInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskMessage;
import devices.glory.GloryDE50Device;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.operation.GloryDE50OperationResponse;
import devices.glory.task.GloryDE50TaskOperation;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class GloryDE50StateOperation extends GloryDE50StateAbstract {

    public GloryDE50StateOperation(GloryDE50Device api) {
        super(api);
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof GloryDE50TaskOperation) {
            GloryDE50TaskOperation opt = (GloryDE50TaskOperation) task;
            api.sendOperation(opt);
        } else if (task instanceof DeviceTaskMessage) {
            DeviceTaskMessage msgt = (DeviceTaskMessage) task;
            GloryDE50OperationInterface op = (GloryDE50OperationInterface) msgt.getMessage();
            DeviceResponseInterface response = msgt.getResponse();
            if (response instanceof GloryDE50OperationResponse) {
                Logger.debug("Got response : %s to operation : %s", response.toString(), op.toString());
            }
            task.setReturnValue(true);
        } else {
            Logger.error("Unexpected task : %s", task.toString());
            task.setReturnValue(false);
        }
        return null;
    }

    @Override
    public String toString() {
        return "GloryDE50StateOperation";
    }

}
