package devices.glory.state;

import devices.device.DeviceResponseInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskReadTimeout;
import devices.device.task.DeviceTaskResponse;
import devices.glory.GloryDE50Device;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.operation.OperationWithAckResponse;
import devices.glory.response.GloryDE50Response;
import devices.glory.response.GloryDE50ResponseError;
import devices.glory.task.GloryDE50TaskOperation;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50StateWaitForResponse extends GloryDE50StateAbstract {

    static public abstract class GloryDE50StateWaitForResponseCallback {

        abstract public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response);

        public DeviceStateInterface onError(final GloryDE50Device api, GloryDE50OperationInterface operation, GloryDE50ResponseError response) {
            return new GloryDE50StateError(api, GloryDE50StateError.COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, response.getError());
        }
    }
    private final GloryDE50TaskOperation opTask;
    private final GloryDE50StateWaitForResponseCallback callBack;

    public GloryDE50StateWaitForResponse(GloryDE50Device api, GloryDE50TaskOperation opTask, GloryDE50StateWaitForResponseCallback callBack) {
        super(api);
        this.opTask = opTask;
        this.callBack = callBack;
    }

    public GloryDE50StateWaitForResponse(final GloryDE50Device api, GloryDE50TaskOperation opTask, final DeviceStateInterface prevStep) {
        super(api);
        this.opTask = opTask;
        this.callBack = new GloryDE50StateWaitForResponseCallback() {

            @Override
            public DeviceStateInterface onResponse(GloryDE50OperationInterface operation, GloryDE50Response response) {
                return prevStep;
            }

        };
    }

    @Override
    public DeviceStateInterface call(DeviceTaskAbstract task) {
        if (task instanceof DeviceTaskResponse) {
            DeviceTaskResponse rTask = (DeviceTaskResponse) task;
            DeviceResponseInterface taskResponse = rTask.getResponse();
            if (taskResponse instanceof GloryDE50Response) {
                OperationWithAckResponse op = (OperationWithAckResponse) opTask.getOperation();
                opTask.setResponse((GloryDE50Response) taskResponse);
                task.setReturnValue(true);
                // give next step the oportunity to process response.
                if (taskResponse instanceof GloryDE50ResponseError) {
                    return callBack.onError(api, op, (GloryDE50ResponseError) taskResponse);
                } else {
                    DeviceStateInterface ret = callBack.onResponse(op, (GloryDE50Response) taskResponse);
                    if (ret == null) {
                        Logger.error("Callback must return a value != null !!!");
                    }
                    return ret;
                }
            } else {
                Logger.error("invalid response type : %s", taskResponse.toString());
                task.setReturnValue(false);
                return null;
            }
        } else if (task instanceof DeviceTaskReadTimeout) {
            opTask.setError("timeout");
            task.setReturnValue(true);
            return new GloryDE50StateError(api, GloryDE50StateError.COUNTER_CLASS_ERROR_CODE.GLORY_APPLICATION_ERROR, "timeout reading from port");
        } else if (task instanceof GloryDE50TaskOperation) {
            GloryDE50TaskOperation opt = (GloryDE50TaskOperation) task;
            opt.setError("Only one operation at a time");
            return null;
        }
        return super.call(task);
    }

    @Override
    public String toString() {
        return "GloryDE50StateWaitForResponse : " + opTask.toString();
    }

}
