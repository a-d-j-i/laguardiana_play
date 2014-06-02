package devices.glory.task;

import devices.device.task.DeviceTaskAbstract;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50OperationResponse;

/**
 *
 * @author adji
 */
public class GloryDE50TaskOperation extends DeviceTaskAbstract {

    final GloryDE50OperationInterface operation;
    final boolean debug;
    String error = null;
    GloryDE50OperationResponse response = new GloryDE50OperationResponse();

    public GloryDE50TaskOperation(Enum type, GloryDE50OperationInterface operation, boolean debug) {
        super(type);
        this.operation = operation;
        this.debug = debug;
    }

    public GloryDE50OperationInterface getOperation() {
        return operation;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setResponse(GloryDE50OperationResponse response) {
        this.response = response;
        setReturnValue(response != null);
    }

    public GloryDE50OperationResponse getResponse() {
        return response;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "GloryDE50TaskOperation{" + "operation=" + operation + ", debug=" + debug + ", error=" + error + ", response=" + response + '}';
    }

}
