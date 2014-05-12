package devices.glory.operation;

import devices.device.operation.DeviceOperationInterface;
import devices.glory.response.GloryDE50OperationResponse;

public interface GloryDE50OperationInterface extends DeviceOperationInterface {

    public String getDescription();

    public GloryDE50OperationResponse getResponse(byte[] dr);
}
