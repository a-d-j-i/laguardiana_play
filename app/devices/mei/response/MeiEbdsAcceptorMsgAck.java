package devices.mei.response;

import devices.mei.MeiEbdsDevice.MessageType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author adji
 */
public class MeiEbdsAcceptorMsgAck implements MeiEbdsAcceptorMsgInterface {

    static final int PAYLOAD_OFFSET = 2;

    enum MEI_EBDS_RESPONSE_BYTE_DESC {

        //BYTE -1 
//BYTE -1 
        ACK(0, 0x01),
        DEVICE_TYPE(0, 0x0E),
        MESSAGE_TYPE(0, 0xF0),
        // THE ONLY CONFIABLE STATED IN BYTE 0: ESCROWED, RETURNED, STACKED
        IDLING(1, 0x01),
        ACECPTING(1, 0x02),
        ESCROWED(1, 0x04),
        STACKING(1, 0x08),
        STACKED(1, 0x10),
        RETURNING(1, 0x20),
        RETURNED(1, 0x40),
        // BYTE1
        CHEATED(2, 0x01),
        REJECTED(2, 0x02),
        JAMMED(2, 0x04),
        CASSETTE_FULL(2, 0x08),
        CASSETTE_INSTALLED(2, 0x10),
        PAUSED(2, 0x20),
        CALIBRATION(2, 0x40),
        // BYTE2
        POWER_UP(3, 0x01),
        INVALID_COMMAND(3, 0x02),
        FAILURE(3, 0x04),
        NOTE_VALUE(3, 0x08 + 0x10 + 0x20),
        // BYTE3
        NO_PUSH_STALLED(4, 0x01),
        FLASH_DOWNLOAD_MODE(4, 0x02),
        PRE_STACK(4, 0x04),
        // BYTE4
        MODEL(5, 0xFF),
        // BYTE5
        REVISION(6, 0xFF),;

        private final int byteNum;
        private final int mask;

        private MEI_EBDS_RESPONSE_BYTE_DESC(int byteNum, int mask) {
            this.byteNum = byteNum + PAYLOAD_OFFSET;
            this.mask = mask;
        }

        private byte getValue(byte[] data) {
            return (byte) ((data[ byteNum] & mask));
        }

        private boolean isSet(byte[] data) {
            return ((data[ byteNum] & mask) != 0);
        }

        private boolean isCleared(byte[] data) {
            return ((data[ byteNum] & mask) == 0);
        }

    }

    byte[] data;

    public MeiEbdsAcceptorMsgAck() {
    }

    public boolean setData(byte[] data) {
        if (data.length != 0x0b) {
            return false;
        }
        if (data[0] != 0x02) {
            return false;
        }
        if (data[ data.length - 2] != 0x03) {
            return false;
        }
        int checksum = 0;
        for (int i = 1; i < data.length - 2; i++) {
            checksum = checksum ^ data[ i];
        }
        if (data[data.length - 1] != (byte) checksum) {
            return false;
        }
        this.data = data;
        return true;
    }

    private static final Map<Integer, MessageType> msgTypeMap = new HashMap<Integer, MessageType>();

    static {
        for (MessageType mt : MessageType.values()) {
            msgTypeMap.put(mt.getId(), mt);
        }
    }

    public MessageType getMessageType() {
        int d = MEI_EBDS_RESPONSE_BYTE_DESC.MESSAGE_TYPE.getValue(data);
        return msgTypeMap.get(d);
    }

    public int getAck() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.ACK.getValue(data);
    }

    public boolean isEscrowed() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.ESCROWED.isSet(data);
    }

    public boolean isReturned() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.RETURNED.isSet(data);
    }

    public boolean isStacked() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.STACKED.isSet(data);
    }

    public boolean isCheated() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.CHEATED.isSet(data);
    }

    public boolean isRejected() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.REJECTED.isSet(data);
    }

    public boolean isJammed() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.JAMMED.isSet(data);
    }

    public boolean isCassetteFull() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.CASSETTE_FULL.isSet(data);
    }

    public boolean isCassetteRemoved() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.CASSETTE_INSTALLED.isSet(data);
    }

    public boolean isPaused() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.PAUSED.isSet(data);
    }

    public boolean isCalibration() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.CALIBRATION.isSet(data);
    }

    public boolean isPowerUp() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.POWER_UP.isSet(data);
    }

    public boolean isInvalidCommand() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.INVALID_COMMAND.isSet(data);
    }

    public boolean isFailure() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.FAILURE.isSet(data);
    }

    public int getNoteValue() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.NOTE_VALUE.getValue(data) >> 3;
    }

    public boolean isNoPushStalled() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.NO_PUSH_STALLED.isSet(data);
    }

    public boolean isFlashDownloadMode() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.FLASH_DOWNLOAD_MODE.isSet(data);
    }

    public boolean isPreStack() {
        return MEI_EBDS_RESPONSE_BYTE_DESC.PRE_STACK.isSet(data);
    }

    @Override
    public String toString() {
        StringBuilder hexString = new StringBuilder();
        hexString.append("\n---------------\n");
        for (byte b : data) {
            hexString.append(" ");
            hexString.append(Integer.toHexString(0xFF & b));
        }
        hexString.append("\n---------------\n");
        for (MEI_EBDS_RESPONSE_BYTE_DESC m : MEI_EBDS_RESPONSE_BYTE_DESC.values()) {
            if (m.isSet(data)) {
                hexString.append(m.name()).append(" : ");
                hexString.append(String.format("0x%x == %d", m.getValue(data), m.getValue(data)));
                hexString.append("\n");
            }
        }
        return "MeiEbdsAcceptorMsg " + hexString.toString();
    }

}
