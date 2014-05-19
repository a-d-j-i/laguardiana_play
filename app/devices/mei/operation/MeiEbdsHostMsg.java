package devices.mei.operation;

import devices.mei.MeiEbdsDevice.MessageType;

/**
 *
 * @author adji
 */
public class MeiEbdsHostMsg {

    static final int PAYLOAD_OFFSET = 2;

    enum MEI_EBDS_MSG_BYTE_DESC {

        //BYTE0
        ACK(0, 0x0F, 0x0F),
        MESSAGE_TYPE(0, 0xF0, 0x00),
        //BYTE1
        DENOMINATION_ENABLE(1, 0xFF, 0xFF),
        //BYTE2
        SPECIAL_INTERRUPT_MODE(2, 0x01, 0x01),
        SECURITY(2, 0x02, 0x02),
        ORIENTATION(2, 0x04 + 0x08, 0),
        ONE_WAY_FACE_UP(2, 0x04 + 0x08, 0),
        TWO_WAY_FACE_UP(2, 0x04 + 0x08, 0x04),
        FOUR_WAY_ALL_DIRECTIONS(2, 0x04 + 0x08, 0x08),
        ESCROW(2, 0x10, 0x10),
        STACK(2, 0x20, 0x20),
        RETURN(2, 0x40, 0x40),
        //BYTE3
        PUSH_MODE(3, 0x01, 0x01),
        BARCODE_ENABLE(3, 0x02, 0x02),
        POWER_UP_A_MODE(3, 0x04 + 0x08, 0x00),
        POWER_UP_B_MODE(3, 0x04, 0x04),
        POWER_UP_C_MODE(3, 0x08, 0x08),
        EXTENDED_NOTE_REPORTING(3, 0x10, 0x10);

        private final int byteNum;
        private final int mask;
        private final int fixedValue;

        private MEI_EBDS_MSG_BYTE_DESC(int byteNum, int mask, Integer fixedValue) {
            this.byteNum = byteNum + PAYLOAD_OFFSET;
            this.mask = mask;
            this.fixedValue = fixedValue;
        }

        private void setBits(byte[] data) {
            data[ byteNum] = (byte) ((data[ byteNum] & ~mask) | (fixedValue & mask));
        }

        private void flipBits(byte[] data) {
            data[ byteNum] = (byte) ((data[ byteNum] & ~mask) | (~data[ byteNum] & mask));
        }

        private void setBit(byte[] data, int bitNum) {
            data[ byteNum] = (byte) (data[ byteNum] | (1 << bitNum));
        }

        private void setValue(byte[] data, int value) {
            data[ byteNum] = (byte) ((data[ byteNum] & ~mask) | (value & mask));
        }

        private void clearBits(byte[] data) {
            data[ byteNum] = (byte) (data[ byteNum] & ~mask);
        }

        private void clearBit(byte[] data, int bitNum) {
            data[ byteNum] = (byte) (data[ byteNum] & (~(1 << bitNum)));
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

    final byte[] data = new byte[8];

    // Reasonble defaults. TODO: Implement all methods.
    public MeiEbdsHostMsg() {
        // Disable all denominations.
        // Standard Host to Acceptor messages
        MEI_EBDS_MSG_BYTE_DESC.MESSAGE_TYPE.setValue(data, 0x10);
        MEI_EBDS_MSG_BYTE_DESC.DENOMINATION_ENABLE.clearBits(data);
        MEI_EBDS_MSG_BYTE_DESC.SPECIAL_INTERRUPT_MODE.setBits(data);
        MEI_EBDS_MSG_BYTE_DESC.SECURITY.setBits(data);
        MEI_EBDS_MSG_BYTE_DESC.FOUR_WAY_ALL_DIRECTIONS.setBits(data);
        // Wait for RETURN or STACK bit in the escrow state.
        MEI_EBDS_MSG_BYTE_DESC.ESCROW.setBits(data);
        // push cheated notes and continue (without issuing any credit)!!!
        MEI_EBDS_MSG_BYTE_DESC.PUSH_MODE.setBits(data);
        //TODO: Implement that : MEI_EBDS_MSG_BYTE_DESC.EXTENDED_NOTE_REPORTING.setBits(data);
        // power sequence must be choosed
    }

    public void enableAllDenominations() {
        MEI_EBDS_MSG_BYTE_DESC.DENOMINATION_ENABLE.setBits(data);
    }

    public void enableDenomination(int denominationId) {
        if (denominationId <= 0 || denominationId > 7) {
            throw new IllegalArgumentException();
        }
        MEI_EBDS_MSG_BYTE_DESC.DENOMINATION_ENABLE.setBit(data, denominationId);
    }

    public void disableAllDenominations() {
        MEI_EBDS_MSG_BYTE_DESC.DENOMINATION_ENABLE.clearBits(data);
    }

    public void disableDenomination(int denominationId) {
        if (denominationId <= 0 || denominationId > 7) {
            throw new IllegalArgumentException();
        }
        MEI_EBDS_MSG_BYTE_DESC.DENOMINATION_ENABLE.clearBit(data, denominationId);
    }

    public void setStackNote() {
        MEI_EBDS_MSG_BYTE_DESC.STACK.setBits(data);
    }

    public void setReturnNote() {
        MEI_EBDS_MSG_BYTE_DESC.STACK.setBits(data);
    }

    public void setAck() {
        MEI_EBDS_MSG_BYTE_DESC.ACK.setBits(data);
    }

    public void flipAck() {
        MEI_EBDS_MSG_BYTE_DESC.ACK.flipBits(data);
    }

    public void clearAck() {
        MEI_EBDS_MSG_BYTE_DESC.ACK.clearBits(data);
    }

    public void setMessageType(MessageType msgType) {
        MEI_EBDS_MSG_BYTE_DESC.MESSAGE_TYPE.setValue(data, msgType.getId());
    }

    public byte[] getCmdStr() {
        data[0] = 0x02;
        data[1] = (byte) data.length;
        data[data.length - 2] = 0x03;
        int checksum = 0;
        for (int i = 1; i < data.length - 2; i++) {
            checksum = checksum ^ data[ i];
        }
        data[data.length - 1] = (byte) checksum;
        return data;
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
        for (MEI_EBDS_MSG_BYTE_DESC m : MEI_EBDS_MSG_BYTE_DESC.values()) {
            if (m.isSet(data)) {
                hexString.append(m.name()).append(" : ");
                hexString.append(String.format("0x%x == %d", m.getValue(data), m.getValue(data)));
                hexString.append("\n");
            }
        }
        return "MeiEbdsHostMsg " + hexString.toString();
    }

}
