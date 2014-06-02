package devices.mei.response;

import devices.device.DeviceMessageInterface;
import devices.mei.MeiEbdsDevice.ExtendedMessageSubType;
import devices.mei.MeiEbdsDevice.MessageSubType;
import devices.mei.MeiEbdsDevice.MessageType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsAcceptorMsgAck implements DeviceMessageInterface {

    enum MEI_EBDS_CMD_DATA_DESC {

        // allways base 0
        ACK(2, 0x01),
        DEVICE_TYPE(2, 0x0E),
        MESSAGE_TYPE(2, 0xF0),
        // allways base 0
        MESSAGE_SUB_TYPE(3, 0xFF),
        // this depends on the 
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
        REVISION(6, 0xFF),
        // EXTENDED TYPES
        NOTE_INDEX(7, 0xFF);
        private final int cmdByte;
        private final int mask;

        private MEI_EBDS_CMD_DATA_DESC(int cmdByte, int mask) {
            this.cmdByte = cmdByte;
            this.mask = mask;
        }

        private byte getValue(byte[] data, int payloadOffset) {
            return (byte) ((data[ cmdByte + payloadOffset] & mask));
        }

        private boolean isSet(byte[] data, int payloadOffset) {
            return ((data[ cmdByte + payloadOffset] & mask) != 0);
        }

        private boolean isCleared(byte[] data, int payloadOffset) {
            return ((data[ cmdByte + payloadOffset] & mask) == 0);
        }

        private int getValue(byte[] data) {
            return getValue(data, 0);
        }

    }

    byte[] data;
    int cdataOffset;
    int length;

    public MeiEbdsAcceptorMsgAck() {
    }

    public boolean setData(int length, byte[] data) {
        this.length = length;
        switch (length) {
            case 0x0b: // normal
                cdataOffset = 2;
                break;
            case 30: // extended
                cdataOffset = 3;
                break;
            default:
                Logger.error("invalid len %d", data[0], length);
                return false;
        }
        if (data[0] != 0x02) {
            Logger.error("invalid stx %d, len %d", data[0], length);
            return false;
        }
        if (data[ length - 2] != 0x03) {
            Logger.error("invalid etx %d, len %d", data[ length - 2], length);
            return false;
        }
        int checksum = 0;
        for (int i = 1; i < length - 2; i++) {
            checksum = checksum ^ data[ i];
        }
        if (data[length - 1] != (byte) checksum) {
            Logger.error("invalid checksum %d != %d, len %d", data[ length - 1], checksum, length);
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

    public MessageType getType() {
        // fixed payload offset
        int d = MEI_EBDS_CMD_DATA_DESC.MESSAGE_TYPE.getValue(data);
        return msgTypeMap.get(d);
    }

    public MessageSubType getMessageSubType() {
        MessageType t = getType();
        if (t == null) {
            return null;
        }
        // fixed payload offset
        return t.getSubType(MEI_EBDS_CMD_DATA_DESC.MESSAGE_SUB_TYPE.getValue(data, 0));
    }

    public int getAck() {
        // fixed payload offset
        return MEI_EBDS_CMD_DATA_DESC.ACK.getValue(data, 0);
    }

    public boolean isEscrowed() {
        return MEI_EBDS_CMD_DATA_DESC.ESCROWED.isSet(data, cdataOffset);
    }

    public boolean isIdling() {
        return MEI_EBDS_CMD_DATA_DESC.IDLING.isSet(data, cdataOffset);
    }

    public boolean isReturned() {
        return MEI_EBDS_CMD_DATA_DESC.RETURNED.isSet(data, cdataOffset);
    }

    public boolean isStacked() {
        return MEI_EBDS_CMD_DATA_DESC.STACKED.isSet(data, cdataOffset);
    }

    public boolean isCheated() {
        return MEI_EBDS_CMD_DATA_DESC.CHEATED.isSet(data, cdataOffset);
    }

    public boolean isRejected() {
        return MEI_EBDS_CMD_DATA_DESC.REJECTED.isSet(data, cdataOffset);
    }

    public boolean isJammed() {
        return MEI_EBDS_CMD_DATA_DESC.JAMMED.isSet(data, cdataOffset);
    }

    public boolean isCassetteFull() {
        return MEI_EBDS_CMD_DATA_DESC.CASSETTE_FULL.isSet(data, cdataOffset);
    }

    public boolean isCassetteRemoved() {
        return MEI_EBDS_CMD_DATA_DESC.CASSETTE_INSTALLED.isSet(data, cdataOffset);
    }

    public boolean isPaused() {
        return MEI_EBDS_CMD_DATA_DESC.PAUSED.isSet(data, cdataOffset);
    }

    public boolean isCalibration() {
        return MEI_EBDS_CMD_DATA_DESC.CALIBRATION.isSet(data, cdataOffset);
    }

    public boolean isPowerUp() {
        return MEI_EBDS_CMD_DATA_DESC.POWER_UP.isSet(data, cdataOffset);
    }

    public boolean isInvalidCommand() {
        return MEI_EBDS_CMD_DATA_DESC.INVALID_COMMAND.isSet(data, cdataOffset);
    }

    public boolean isFailure() {
        return MEI_EBDS_CMD_DATA_DESC.FAILURE.isSet(data, cdataOffset);
    }

    public String getNoteSlot() {
        MessageType t = getType();
        if (t != null && t == MessageType.Extended) {
            MessageSubType st = getMessageSubType();
            if (st != null && st == ExtendedMessageSubType.RequestSupportedNoteSet) {
                return new String(Arrays.copyOfRange(data, 11, 20));
            }
        }
        return Integer.toString(MEI_EBDS_CMD_DATA_DESC.NOTE_VALUE.getValue(data, cdataOffset) >> 3);
    }

    public boolean isNoPushStalled() {
        return MEI_EBDS_CMD_DATA_DESC.NO_PUSH_STALLED.isSet(data, cdataOffset);
    }

    public boolean isFlashDownloadMode() {
        return MEI_EBDS_CMD_DATA_DESC.FLASH_DOWNLOAD_MODE.isSet(data, cdataOffset);
    }

    public boolean isPreStack() {
        return MEI_EBDS_CMD_DATA_DESC.PRE_STACK.isSet(data, cdataOffset);
    }

    @Override
    public String toString() {
        if (data == null || length == 0) {
            return "Empty data";
        }
        StringBuilder hexString = new StringBuilder();
        hexString.append("\n---------------\n");
        hexString.append("Payload offset : ").append(cdataOffset).append("\n");
        for (int i = 0; i < length; i++) {
            hexString.append(" ").append(String.format("%2x", 0xFF & data[i]));
        }
        hexString.append("\n");
        for (int i = 0; i < length; i++) {
            if (data[i] > 31 && data[i] < 127) {
                hexString.append("  ").append((char) (0xFF & data[i]));
            } else {
                hexString.append(" ").append(String.format("%2d", 0xFF & data[i]));
            }
        }
        hexString.append("\n---------------\n");
        for (MEI_EBDS_CMD_DATA_DESC m : MEI_EBDS_CMD_DATA_DESC.values()) {
            switch (m) {
                case ACK:
                case DEVICE_TYPE:
                case MESSAGE_TYPE:
                case MESSAGE_SUB_TYPE:
                    hexString.append(m.name()).append(" : ");
                    hexString.append(String.format("0x%x == %d", m.getValue(data), m.getValue(data)));
                    hexString.append("\n");
                    break;
                default:
                    if (m.isSet(data, cdataOffset)) {
                        hexString.append(m.name()).append(" : ");
                        hexString.append(String.format("0x%x == %d", m.getValue(data, cdataOffset), m.getValue(data, cdataOffset)));
                        hexString.append("\n");
                    }
            }
        }
        return "MeiEbdsAcceptorMsg " + hexString.toString();
    }

}
