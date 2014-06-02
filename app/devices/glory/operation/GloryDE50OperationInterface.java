package devices.glory.operation;

import devices.glory.response.GloryDE50OperationResponse;

public interface GloryDE50OperationInterface {

    public String getDescription();

    public byte[] getCmdStr();

    public String fillResponse(int len, byte[] b, GloryDE50OperationResponse response);
}
