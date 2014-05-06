package devices.glory.command;

import devices.glory.response.GloryDE50Response.D1Mode;
import devices.glory.response.GloryDE50Response.ResponseRepr;
import devices.glory.response.GloryDE50Response.SR1Mode;
import play.Logger;

public class OperationdWithDataResponse extends OperationWithAckResponse {

    OperationdWithDataResponse(byte cmdId, String description) {
        this(cmdId, description, null, DebugLevel.NONE);
    }

    OperationdWithDataResponse(byte cmdId, String description, byte[] cmdData) {
        this(cmdId, description, cmdData, DebugLevel.NONE);
    }

    OperationdWithDataResponse(byte cmdId, String description, byte[] cmdData, DebugLevel debug) {
        super(cmdId, description, cmdData, debug);
    }

    @Override
    public void setResponse(byte[] dr) {
        if (dr == null) {
            response.setError("Invalid argument dr == null");
            return;
        }
        int l = dr.length;

        if (l == 1) {
            super.setResponse(dr);
            return;
        } else {
            byte[] sdr = new byte[1];
            sdr[ 0] = 0x6;
            super.setResponse(sdr);
        }
        if (response.getError() != null) {
            return;
        }
        if (dr.length < 21) {
            response.setError(String.format("Invalid command (%s) response length %d expected ack/noack", getDescription(),
                    dr.length));
            return;
        }

        if (dr[ l - 2] != 3) {
            response.setError(String.format("Invalid command (%s) message end not found", getDescription()));
            return;
        }

        byte retCs = 0;
        for (int i = 0; i < l - 1; i++) {
            retCs = (byte) (retCs ^ dr[ i]);
        }

        if (dr[ l - 1] != (byte) retCs) {
            response.setError(String.format("CHECKSUM don't match 0x%x != 0x%x", dr[ l - 1], retCs));
            return;
        }

        response.setSr1Mode(SR1Mode.getMode(dr[ 3] & 0x3F));
        response.setSr2((byte) (dr[ 4] & 0x3F));
        response.setSr3((byte) (dr[ 5] & 0x3F));
        response.setSr4((byte) (dr[ 6] & 0x0F));

        if (l - 21 > 0) {
            byte[] data = new byte[l - 21];
            System.arraycopy(dr, 7, data, 0, l - 21);
            response.setData(data);
        }

        response.setD1Mode(D1Mode.getMode(dr[ l - 14] & 0x1F));
        response.setD2((byte) (dr[ l - 13] & 0x3F));
        response.setD3((byte) (dr[ l - 12] & 0x07));
        response.setD4((byte) (dr[ l - 11] & 0x3F));
        response.setD5((byte) (dr[ l - 10] & 0x07));
        response.setD6((byte) (dr[ l - 9] & 0x07));
        response.setD7((byte) (dr[ l - 8] & 0x07));
        response.setD8((byte) (dr[ l - 7] & 0x07));
        response.setD9((byte) (dr[ l - 6] & 0x7F));
        response.setD10((byte) (dr[ l - 5] & 0x7F));
        response.setD11((byte) (dr[ l - 4] & 0x7F));
        response.setD12((byte) (dr[ l - 3] & 0x7F));

        if (debug.isGratherThan(DebugLevel.NONE)) {
            ResponseRepr s = response.repr();
            Logger.debug(s.SR1Mode);
            for (String ss : s.getSrBits()) {
                Logger.debug(ss);
            }
            //Logger.debug(s.getD1Mode());
            if (debug.isGratherThan(DebugLevel.DEBUG)) {
                for (String ss : s.getD2Bits()) {
                    Logger.debug(ss);
                }
                for (String ss : s.getInfo()) {
                    Logger.debug(ss);
                }
            }
        }
        return;
    }
}
