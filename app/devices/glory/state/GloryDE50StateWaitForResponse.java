package devices.glory.state;

import devices.device.DeviceResponseInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskReadTimeout;
import devices.device.task.DeviceTaskResponse;
import devices.glory.GloryDE50Device;
import devices.glory.response.GloryDE50ResponseWithData;
import devices.glory.response.GloryDE50AcceptorMsg;
import devices.glory.task.GloryDE50TaskOperation;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50StateWaitForResponse extends GloryDE50StateAbstract {

    private final GloryDE50TaskOperation opTask;
    private final GloryDE50StateAbstract nextStep;

    public GloryDE50StateWaitForResponse(GloryDE50Device api, GloryDE50TaskOperation opTask, GloryDE50StateAbstract nextStep) {
        super(api);
        this.opTask = opTask;
        this.nextStep = nextStep;
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskResponse) {
            DeviceTaskResponse rTask = (DeviceTaskResponse) task;
            DeviceResponseInterface taskResponse = rTask.getResponse();
            if (taskResponse instanceof GloryDE50AcceptorMsg) {
                GloryDE50AcceptorMsg response = (GloryDE50AcceptorMsg) taskResponse;
                opTask.fillResponse(response);
                task.setReturnValue(true);
                return nextStep;
            } else {
                Logger.error("invalid response type : %s", taskResponse.toString());
                task.setReturnValue(false);
                return null;
            }
        } else if (task instanceof DeviceTaskReadTimeout) {
            opTask.setError("timeout");
            task.setReturnValue(true);
            return new GloryDE50StateOpenPort(api);
        } else if (task instanceof GloryDE50TaskOperation) {
            GloryDE50TaskOperation opt = (GloryDE50TaskOperation) task;
            opt.setError("Only one operation at a time");
            return null;
        }
        return super.call(task);
    }

    @Override
    public String toString() {
        return "GloryDE50StateWaitForResponse";
    }

}
