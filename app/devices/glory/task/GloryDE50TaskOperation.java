package devices.glory.task;

import devices.device.task.DeviceTaskAbstract;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.operation.OperationWithAckResponse;
import devices.glory.response.GloryDE50Response;
import devices.glory.response.GloryDE50ResponseError;

/**
 *
 * @author adji
 */
public class GloryDE50TaskOperation extends DeviceTaskAbstract {

    // access internal data.
    final OperationWithAckResponse operation;
    final boolean debug;

    public GloryDE50TaskOperation(GloryDE50OperationInterface operation, boolean debug) {
        this.operation = (OperationWithAckResponse) operation;
        this.debug = debug;
    }

    public GloryDE50OperationInterface getOperation() {
        return operation;
    }

    public void setError(String err) {
        operation.setResponse(new GloryDE50ResponseError(err));
        setReturnValue(false);
    }

    public void setResponse(GloryDE50Response response) {
        operation.setResponse(response);
        setReturnValue(true);
    }

    public GloryDE50Response getResponse() {
        return operation.getResponse();
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    public String toString() {
        return "GloryDE50TaskOperation{" + "operation=" + operation + "}";
    }

}
