package devices.glory.operation;

import devices.glory.response.GloryDE50Response;
import devices.glory.response.GloryDE50ResponseError;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import play.Logger;

public class OperationWithAckResponse implements GloryDE50OperationInterface {

    protected byte cmdId;

    public OperationWithAckResponse(int cmdId) {
        this.cmdId = (byte) cmdId;
    }

    @Override
    public String toString() {
        return String.format("CMD 0x%x %s", cmdId, this.getClass().getSimpleName());
    }

    public String getDescription() {
        return this.getClass().getSimpleName();
    }
    GloryDE50Response response = new GloryDE50ResponseError("Invalid response, command failed");

    public void setResponse(GloryDE50Response response) {
        response.setOperation(this);
        this.response = response;
    }

    @Override
    public GloryDE50Response getResponse() {
        return response;
    }

    public byte[] getCmdStr() {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();

        try {
            bo.write(2);
            //Logger.debug(String.format("Executing command 0x%x %s", cmdId, this.toString()));
            bo.write(String.format("%03d", 1).getBytes());
            bo.write(cmdId);
            bo.write(3);
            byte cs = 2;
            for (byte x : bo.toByteArray()) {
                cs = (byte) (cs ^ (byte) x);
            }
            bo.write(cs);
            bo.close();
        } catch (IOException e) {
            Logger.error("Error in getCmdStr exception %s", e);
        }
        return bo.toByteArray();
    }

    public byte[] getCmdStrFromData(byte[] cmdData) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();

        try {
            bo.write(2);

            if (cmdData.length <= 999) {
                bo.write(String.format("%03d", cmdData.length + 1).getBytes());
            } else if (cmdData.length <= 0xFFF) {
                bo.write(getXXFormat(cmdData.length + 1, 0x70, 3));
            } else {
                throw new IllegalArgumentException(String.format("Error in getCmdStr invalid cmd len %d", cmdData.length));
            }
            bo.write(cmdId);
            bo.write(cmdData);
            bo.write(3);
            byte cs = 2;
            for (byte x : bo.toByteArray()) {
                cs = (byte) (cs ^ (byte) x);
            }
            bo.write(cs);
            bo.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error in getCmdStr");
        }
        return bo.toByteArray();
    }

    protected byte[] getXXFormat(long data, int base, int len) {
        if (len < 1) {
            throw new IllegalArgumentException(String.format("Invalid len %d", len));
            //len = 1;
        }
        if (data >= ((long) 1 << (len * 4 + 1))) {
            throw new IllegalArgumentException(String.format("Invalid data 0x%x for len %d", data, len));
            //data = ((long) 1 << (len * 4 + 1)) - 1;
        }
        byte[] ret = new byte[len];
        for (int i = 0; len > 0; i += 4) {
            long m = ((long) 0xF) << i;
            ret[--len] = (byte) (((data & m) >>> i) + base);
        }
        return ret;
    }

}
