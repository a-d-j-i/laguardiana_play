package devices.glory.task;

import devices.device.task.DeviceTaskAbstract;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50AcceptorMsg;
import devices.glory.response.GloryDE50Response;
import devices.glory.response.GloryDE50ResponseError;

/**
 *
 * @author adji
 */
public class GloryDE50TaskOperation extends DeviceTaskAbstract {

    final GloryDE50OperationInterface operation;
    final boolean debug;
    GloryDE50Response response;

    public GloryDE50TaskOperation(GloryDE50OperationInterface operation, boolean debug) {
        this.operation = operation;
        this.debug = debug;
    }

    public GloryDE50OperationInterface getOperation() {
        return operation;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setError(String error) {
        this.response = new GloryDE50ResponseError(error);
        setReturnValue(false);
    }

    public void fillResponse(GloryDE50AcceptorMsg msg) {
        response = operation.getResponse(msg.getLength(), msg.getData());
        setReturnValue(true);
    }

    public GloryDE50Response getResponse() {
        if (!isDone()) {
            return null;
        }
        return response;
    }

    @Override
    public String toString() {
        return "GloryDE50TaskOperation{" + "operation=" + operation + "}";
    }

}
