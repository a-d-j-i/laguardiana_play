package devices.glory.operation;

import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.response.GloryDE50OperationResponse.D1Mode;
import devices.glory.response.GloryDE50OperationResponse.SR1Mode;

public class OperationdWithDataResponse extends OperationWithAckResponse {

    public OperationdWithDataResponse(int cmdId) {
        super(cmdId);
    }

    @Override
    public String fillResponse(int len, byte[] dr, final GloryDE50OperationResponse response) {
        if (dr == null) {
            return "Invalid argument dr == null";
        }
        int l = len;

        if (l == 1) {
            return super.fillResponse(len, dr, response);
        }
        if (len < 2) {
            return String.format("Invalid command (%s) response length %d expected ack/noack", getDescription(), len);
        }

        if (dr[ l - 2] != 3) {
            return String.format("Invalid command (%s) message end not found", getDescription());
        }

        byte checksum = 0;
        for (int i = 0; i < l - 1; i++) {
            checksum = (byte) (checksum ^ dr[ i]);
        }

        if (dr[ l - 1] != (byte) checksum) {
            return String.format("CHECKSUM don't match 0x%x != 0x%x", dr[ l - 1], checksum);
        }

//        String err = super.fillResponse(1, dr, response);
//        if (err != null) {
//            return err;
//        }
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
        return null;
    }

    protected byte getDecDigit(byte l) {
        if (l >= 0x30 && l <= 0x39) {
            return (byte) (l - 0x30);
        }
        throw new NumberFormatException(String.format("invalid digit 0x%x", l));

    }

    protected byte getHexDigit(byte l) {
        if (l >= 0x30 && l <= 0x3F) {
            return (byte) (l - 0x30);
        }
        throw new NumberFormatException(String.format("invalid digit 0x%x", l));
    }

}
