package devices.glory.response;

import devices.glory.operation.OperationWithAckResponse;

/**
 *
 * @author adji
 */
public class GloryDE50ResponseNak extends GloryDE50ResponseError {

    public GloryDE50ResponseNak() {
        super("Command not acknowledged");
    }

    @Override
    public void setOperation(OperationWithAckResponse operation) {
        super.setOperation(operation);
        error = String.format("Command %s not acknowledged", operation.getDescription());
    }

    @Override
    public String toString() {
        return "GloryDE50ResponseNak";
    }

}
