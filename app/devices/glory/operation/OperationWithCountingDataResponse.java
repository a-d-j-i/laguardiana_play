package devices.glory.operation;

import devices.glory.response.GloryDE50OperationResponse;
import play.Logger;

public class OperationWithCountingDataResponse extends OperationdWithDataResponse {

    public OperationWithCountingDataResponse(int cmdId) {
        super(cmdId);
    }

    @Override
    public String fillResponse(int len, byte[] dr, final GloryDE50OperationResponse response) {
        String err = super.fillResponse(len, dr, response);
        if (err != null) {
            return err;
        }

        byte[] data = response.getData();
        if (data == null || data.length == 0) {
            return "Response invalid data";
        }

        if (data.length == 32 * 3) {
            for (int slot = 0; slot < 32; slot++) {
                Integer value = getDecDigit(data[ 3 * slot]) * 100
                        + getDecDigit(data[ 3 * slot + 1]) * 10
                        + getDecDigit(data[ 3 * slot + 2]);
                response.setBill(slot, value);
                Logger.debug(String.format("readed bill %d %d", slot, value));
                Logger.debug(String.format("Bill %d: quantity %d", slot, value));
            }
        } else if (data.length == 65 * 4) {
            for (int slot = 0; slot < 65; slot++) {
                Integer value = getHexDigit(data[ 4 * slot]) * 1000
                        + getHexDigit(data[ 4 * slot + 1]) * 100
                        + getHexDigit(data[ 4 * slot + 2]) * 10
                        + getHexDigit(data[ 4 * slot + 3]);
                response.setBill(slot, value);
                Logger.debug(String.format("readed bill %d %d", slot, value));
                Logger.debug(String.format("Bill %d: quantity %d", slot, value));
            }
        } else {
            return String.format("Invalid command (%s) response length %d expected", getDescription(), len);
        }
        return null;
    }
}
