package devices.glory.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import play.Logger;

public abstract class GloryCommandAbstract {

    private byte cmdId;
    private String description;
    private byte[] cmdData;
    IOException error = null;

    public enum DebugLevel {

        NONE(0), DEBUG(1), PRINT_INFO(2);
        private final int level;

        DebugLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        public boolean isGratherThan(DebugLevel level) {
            return this.getLevel() > level.getLevel();
        }
    }
    DebugLevel debug = DebugLevel.NONE;

    GloryCommandAbstract(byte cmdId, String description) {
        this(cmdId, description, null, DebugLevel.NONE);
    }

    GloryCommandAbstract(byte cmdId, String description, byte[] cmdData) {
        this(cmdId, description, cmdData, DebugLevel.NONE);
    }

    GloryCommandAbstract(byte cmdId, String description, byte[] cmdData, DebugLevel debug) {
        this.cmdData = cmdData;
        this.cmdId = cmdId;
        this.description = description;
        this.debug = debug;
    }

    public byte[] getCmdStr() {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();

        try {
            bo.write(2);

            Logger.debug(String.format("Executing command 0x%x %s", cmdId, this.toString()));
            if (cmdData == null) {
                bo.write(String.format("%03d", 1).getBytes());
                bo.write(cmdId);
                bo.write(3);
            } else {
                if (cmdData.length < 1000) {
                    bo.write(String.format("%03d", cmdData.length + 1).getBytes());
                } else {
                    bo.write(String.format("%03X", cmdData.length + 1).getBytes());
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

    public void printCmd() {
        if (debug.isGratherThan(DebugLevel.NONE)) {
            Logger.debug(String.format("CMD 0x%x %s", cmdId, description));
        }
    }

    protected byte getDigit(byte l) {
        if (l >= 0x30 && l <= 0x39) {
            return (byte) (l - 0x30);
        }
        if (l >= 0x3a && l <= 0x3f) {
            return (byte) (l - 0x3a + 10);
        }
        // 'A' - 'F'
        if (l >= 0x41 && l <= 0x46) {
            return (byte) (l - 0x40 + 10);
        }
        // 'a' - 'f'
        if (l >= 0x61 && l <= 0x66) {
            return (byte) (l - 0x60 + 10);
        }
        setError(String.format("invalid digit 0x%x", l));
        return 0;

    }

    public GloryCommandAbstract setResult(byte[] dr) {
        return this;
    }
}
