package devices.glory.command;

import devices.glory.GloryReturnParser;
import devices.glory.GloryState.D1Mode;
import devices.glory.GloryState.SR1Mode;
import play.Logger;

public class CommandWithDataResponse extends CommandWithAckResponse {

    SR1Mode sr1 = SR1Mode.unknown;
    byte sr2;
    byte sr3;
    byte sr4;
    byte[] data = null;
    D1Mode d1 = D1Mode.unknown;
    byte d2;
    byte d3;
    byte d4;
    byte d5;
    byte d6;
    byte d7;
    byte d8;
    byte d9;
    byte d10;
    byte d11;
    byte d12;

    CommandWithDataResponse(byte cmdId, String description) {
        this(cmdId, description, null, DebugLevel.NONE);
    }

    CommandWithDataResponse(byte cmdId, String description, byte[] cmdData) {
        this(cmdId, description, cmdData, DebugLevel.NONE);
    }

    CommandWithDataResponse(byte cmdId, String description, byte[] cmdData, DebugLevel debug) {
        super(cmdId, description, cmdData, debug);
    }

    @Override
    public CommandWithDataResponse setResult(byte[] dr) {
        if (dr == null) {
            setError("Invalid argument dr == null");
            return this;
        }
        int l = dr.length;

        if (l == 1) {
            return (CommandWithDataResponse) super.setResult(dr);
        } else {
            byte[] sdr = new byte[1];
            sdr[ 0] = 0x6;
            super.setResult(sdr);
        }
        if (getError() != null) {
            return this;
        }
        if (dr.length < 21) {
            setError(String.format("Invalid command (%s) response length %d expected ack/noack", getDescription(),
                    dr.length));
            return this;
        }

        if (dr[ l - 2] != 3) {
            setError(String.format("Invalid command (%s) message end not found", getDescription()));
            return this;
        }

        byte retCs = 0;
        for (int i = 0; i < l - 1; i++) {
            retCs = (byte) (retCs ^ dr[ i]);
        }

        if (dr[ l - 1] != (byte) retCs) {
            setError(String.format("CHECKSUM don't match 0x%x != 0x%x", dr[ l - 1], retCs));
            return this;
        }

        sr1 = SR1Mode.getMode(dr[ 3] & 0x3F);
        sr2 = (byte) (dr[ 4] & 0x3F);
        sr3 = (byte) (dr[ 5] & 0x3F);
        sr4 = (byte) (dr[ 6] & 0x0F);

        if (l - 21 > 0) {
            data = new byte[l - 21];
            System.arraycopy(dr, 7, data, 0, l - 21);
        }

        d1 = D1Mode.getMode(dr[ l - 14] & 0x1F);
        d2 = (byte) (dr[ l - 13] & 0x3F);
        d3 = (byte) (dr[ l - 12] & 0x07);
        d4 = (byte) (dr[ l - 11] & 0x3F);
        d5 = (byte) (dr[ l - 10] & 0x07);
        d6 = (byte) (dr[ l - 9] & 0x07);
        d7 = (byte) (dr[ l - 8] & 0x07);
        d8 = (byte) (dr[ l - 7] & 0x07);
        d9 = (byte) (dr[ l - 6] & 0x7F);
        d10 = (byte) (dr[ l - 5] & 0x7F);
        d11 = (byte) (dr[ l - 4] & 0x7F);
        d12 = (byte) (dr[ l - 3] & 0x7F);

        if (debug.isGratherThan(DebugLevel.NONE)) {
            GloryReturnParser s = new GloryReturnParser(this);
            Logger.debug(s.getSRMode());
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
        return this;
    }

    public SR1Mode getSr1() {
        return sr1;
    }

    public byte getSr2() {
        return sr2;
    }

    public byte getSr3() {
        return sr3;
    }

    public byte getSr4() {
        return sr4;
    }

    public D1Mode getD1() {
        return d1;
    }

    public byte getD2() {
        return d2;
    }

    public byte getD3() {
        return d3;
    }

    public byte getD4() {
        return d4;
    }

    public byte getD5() {
        return d5;
    }

    public byte getD6() {
        return d6;
    }

    public byte getD7() {
        return d7;
    }

    public byte getD8() {
        return d8;
    }

    public byte getD9() {
        return d9;
    }

    public byte getD10() {
        return d10;
    }

    public byte getD11() {
        return d11;
    }

    public byte getD12() {
        return d12;
    }

    public byte[] getData() {
        return data;
    }
}
