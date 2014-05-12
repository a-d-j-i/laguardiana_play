/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.operation;

import devices.mei.MeiEbds.MessageType;

/**
 *
 * @author adji
 */
class MeiEbdsHostMsg {

    static final int PAYLOAD_OFFSET = 2;

    enum MEI_DBDS_MSG_BYTE_DESC {

        //BYTE0
        ACK(0, 0x0F, 0x0F),
        MESSAGE_TYPE(0, 0xF0, 0x00),
        //BYTE1
        DENOMINATION_ENABLE(1, 0xFF, 0xFF),
        //BYTE2
        SPECIAL_INTERRUPT_MODE(2, 0x01, 0x01),
        SECURITY(2, 0x02, 0x02),
        ORIENTATION(2, 0x04 + 0x08, null),
        ONE_WAY_FACE_UP(2, 0x04 + 0x08, 0),
        TWO_WAY_FACE_UP(2, 0x04 + 0x08, 0x04),
        FOUR_WAY_ALL_DIRECTIONS(2, 0x04 + 0x08, 0x08),
        ESCROW(2, 0x10, 0x10),
        STACK(2, 0x20, 0x20),
        RETURN(2, 0x40, 0x40),
        //BYTE3
        PUSH_MODE(3, 0x01, 0x01),
        BARCODE_ENABLE(3, 0x02, 0x02),
        POWER_UP_B_MODE(3, 0x04, 0x04),
        POWER_UP_C_MODE(3, 0x08, 0x08),
        EXTENDED_NOTE_REPORTING(3, 0x10, 0x10);

        private final int byteNum;
        private final int mask;
        private final int fixedValue;

        private MEI_DBDS_MSG_BYTE_DESC(int byteNum, int mask, Integer fixedValue) {
            this.byteNum = byteNum + PAYLOAD_OFFSET;
            this.mask = mask;
            this.fixedValue = fixedValue;
        }

        private void setBits(byte[] data) {
            data[ byteNum] = (byte) ((data[ byteNum] & ~mask) | (fixedValue & mask));
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

    }

    byte[] data = new byte[3];

    // Reasonble defaults. TODO: Implement all methods.
    public MeiEbdsHostMsg() {
        // Disable all denominations.
        MEI_DBDS_MSG_BYTE_DESC.DENOMINATION_ENABLE.clearBits(data);
        MEI_DBDS_MSG_BYTE_DESC.SPECIAL_INTERRUPT_MODE.setBits(data);
        MEI_DBDS_MSG_BYTE_DESC.SECURITY.setBits(data);
        MEI_DBDS_MSG_BYTE_DESC.FOUR_WAY_ALL_DIRECTIONS.setBits(data);
        // Wait for RETURN or STACK bit in the escrow state.
        MEI_DBDS_MSG_BYTE_DESC.ESCROW.setBits(data);
        // push cheated notes and continue (without issuing any credit)!!!
        MEI_DBDS_MSG_BYTE_DESC.PUSH_MODE.setBits(data);
        // power sequence must be choosed
    }

    public void enableAllDenominations() {
        MEI_DBDS_MSG_BYTE_DESC.DENOMINATION_ENABLE.setBits(data);
    }

    public void enableDenomination(int denominationId) {
        if (denominationId <= 0 || denominationId > 7) {
            throw new IllegalArgumentException();
        }
        MEI_DBDS_MSG_BYTE_DESC.DENOMINATION_ENABLE.setBit(data, denominationId);
    }

    public void disableAllDenominations() {
        MEI_DBDS_MSG_BYTE_DESC.DENOMINATION_ENABLE.clearBits(data);
    }

    public void disableDenomination(int denominationId) {
        if (denominationId <= 0 || denominationId > 7) {
            throw new IllegalArgumentException();
        }
        MEI_DBDS_MSG_BYTE_DESC.DENOMINATION_ENABLE.clearBit(data, denominationId);
    }

    public void setStackNote() {
        MEI_DBDS_MSG_BYTE_DESC.STACK.setBits(data);
    }

    public void setReturnNote() {
        MEI_DBDS_MSG_BYTE_DESC.STACK.setBits(data);
    }

    public void setAck() {
        MEI_DBDS_MSG_BYTE_DESC.ACK.setBits(data);
    }

    public void clearAck() {
        MEI_DBDS_MSG_BYTE_DESC.ACK.clearBits(data);
    }

    public void setMessageType(MessageType msgType) {
        MEI_DBDS_MSG_BYTE_DESC.MESSAGE_TYPE.setValue(data, msgType.getId());
    }

}
