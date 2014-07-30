package devices.glory.task;

import devices.device.task.DeviceTaskAbstract;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.operation.GloryDE50OperationResponse;

/**
 *
 * @author adji
 */
public class GloryDE50TaskOperation extends DeviceTaskAbstract {

    final GloryDE50OperationInterface operation;
    final boolean debug;
    // this could be atomic if needed.
    String error = null;
    GloryDE50OperationResponse response = new GloryDE50OperationResponse();

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

    public void setResponse(String error, GloryDE50OperationResponse response) {
        this.error = error;
        this.response = response;
        setReturnValue(response != null);
    }

    public GloryDE50OperationResponse getResponse() {
        if (!isDone()) {
            return null;
        }
        return response;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isError() {
        if (!isDone()) {
            return true;
        }
        return error != null;
    }

    public String getError() {
        if (!isDone()) {
            return "not done";
        }
        return error;
    }

    @Override
    public String toString() {
        return "GloryDE50TaskOperation{" + "operation=" + operation + "}";
    }

}
