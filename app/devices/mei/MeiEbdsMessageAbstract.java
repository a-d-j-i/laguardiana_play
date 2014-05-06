/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import play.Logger;

/**
 *
 * @author adji
 */
class MeiEbdsMessageAbstract {

    private byte cmdId;
    private String description;
    private byte[] cmdData;
    IOException error = null;

    MeiEbdsMessageAbstract(byte cmdId, String description) {
        this(cmdId, description, null);
    }

    MeiEbdsMessageAbstract(byte cmdId, String description, byte[] cmdData) {
        this.cmdData = cmdData;
        this.cmdId = cmdId;
        this.description = description;
    }

    protected byte[] getXXFormat(long data, int base, int len) {
        if (len < 1) {
            setError(String.format("Invalid len %d", len));
            len = 1;
        }
        if (data >= ((long) 1 << (len * 4 + 1))) {
            setError(String.format("Invalid data 0x%x for len %d", data, len));
            data = ((long) 1 << (len * 4 + 1)) - 1;
        }
        byte[] ret = new byte[len];
        for (int i = 0; len > 0; i += 4) {
            long m = ((long) 0xF) << i;
            ret[--len] = (byte) (((data & m) >>> i) + base);
        }
        return ret;
    }

    public byte[] getCmdStr() {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();

        try {
            bo.write(2);

            //Logger.debug(String.format("Executing command 0x%x %s", cmdId, this.toString()));
            if (cmdData == null) {
                bo.write(String.format("%03d", 1).getBytes());
                bo.write(cmdId);
                bo.write(3);
            } else {
                if (cmdData.length <= 999) {
                    bo.write(String.format("%03d", cmdData.length + 1).getBytes());
                } else if (cmdData.length <= 0xFFF) {
                    bo.write(getXXFormat(cmdData.length + 1, 0x70, 3));
                } else {
                    setError(String.format("Error in getCmdStr invalid cmd len %d", cmdData.length));
                }
                bo.write(cmdId);
                bo.write(cmdData);
                bo.write(3);
            }
            byte cs = 2;
            for (byte x : bo.toByteArray()) {
                cs = (byte) (cs ^ (byte) x);
            }
            bo.write(cs);
            bo.close();
        } catch (IOException e) {
            setError("Error in getCmdStr");
        }
        return bo.toByteArray();
    }

    public void setError(String msg) {
        this.error = new IOException(msg, this.error);
    }

    public String getError() {
        if (this.error == null) {
            return null;
        }
        Logger.error(String.format("Error : %s", this.error));
        return this.error.getMessage();
    }

    public DebugLevel getDebug() {
        return debug;
    }

    public void setDebug(DebugLevel debug) {
        this.debug = debug;
    }

    public byte getId() {
        return cmdId;
    }

    public String getDescription() {
        return description;
    }

    public void setCmdData(byte[] cmdData) {
        this.cmdData = cmdData;
    }

    protected byte getDecDigit(byte l) {
        if (l >= 0x30 && l <= 0x39) {
            return (byte) (l - 0x30);
        }
        setError(String.format("invalid digit 0x%x", l));
        return 0;

    }

    protected byte getHexDigit(byte l) {
        if (l >= 0x30 && l <= 0x3F) {
            return (byte) (l - 0x30);
        }
        setError(String.format("invalid digit 0x%x", l));
        return 0;

    }

    public MeiEbdsMessageAbstract setResult(byte[] dr) {
        return this;
    }

    @Override
    public String toString() {
        return String.format("CMD 0x%x %s", cmdId, description);
    }

}
