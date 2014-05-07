package devices.glory.operation;

import play.Logger;

public class OperationWithAckResponse extends GloryOperationAbstract {

    OperationWithAckResponse(byte cmdId, String description) {
        this(cmdId, description, null, DebugLevel.NONE);
    }

    OperationWithAckResponse(byte cmdId, String description, byte[] cmdData) {
        this(cmdId, description, cmdData, DebugLevel.NONE);
    }

    OperationWithAckResponse(byte cmdId, String description, byte[] cmdData, DebugLevel debug) {
        super(cmdId, description, cmdData, debug);
    }

    public void setResponse(byte[] dr) {

        if (dr.length != 1) {
            response.setError(String.format("Invalid command (%s) response length %d expected ack/noack", getDescription(), dr.length));
            return;
        }
        if (dr[ 0] == 0x6) {
            if (debug.isGratherThan(DebugLevel.NONE)) {
                Logger.debug(String.format("Command %s ack", getDescription()));
            }
            return;
        } else if (dr[ 0] == 0x15) {
            response.setError(String.format("Command %s not acknowledged", getDescription()));
            return;
        }
        response.setError(String.format("Invalid command (%s) response expected ack/noack 0x%x", getDescription(), dr[ 0]));
        return;
    }

}
