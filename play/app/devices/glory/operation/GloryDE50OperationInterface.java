package devices.glory.operation;

import devices.device.DeviceMessageInterface;
import devices.glory.response.GloryDE50Response;

public interface GloryDE50OperationInterface extends DeviceMessageInterface {

    public String getDescription();

    public byte[] getCmdStr();

    public GloryDE50Response getResponse();
}
