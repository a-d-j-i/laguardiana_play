package devices.glory.response;

import devices.device.DeviceResponseInterface;
import devices.glory.operation.OperationWithAckResponse;
import java.io.Serializable;

/**
 *
 * @author adji
 */
public class GloryDE50Response implements Serializable, DeviceResponseInterface {

    transient protected OperationWithAckResponse operation = null;

    @Override
    public String toString() {
        return "GloryDE50Response";
    }

    public boolean isError() {
        return false;
    }

    public boolean hasData() {
        return false;
    }

    public void setOperation(OperationWithAckResponse operation) {
        this.operation = operation;
    }
}
