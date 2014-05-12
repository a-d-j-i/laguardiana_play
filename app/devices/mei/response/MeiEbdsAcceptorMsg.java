/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei.response;

import devices.mei.MeiEbds.MessageType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author adji
 */
class MeiEbdsAcceptorMsg {

    static final int PAYLOAD_OFFSET = 2;

    enum MEI_DBDS_RESPONSE_BYTE_DESC {

        //BYTE0
        ACK(0, 0x0F),
        MESSAGE_TYPE(0, 0xF0),
        // THE ONLY CONFIABLE STATED IN BYTE 1: ESCROWED, RETURNED, STACKED
        IDLING(1, 0x01),
        ACECPTING(1, 0x02),
        ESCROWED(1, 0x04),
        STACKING(1, 0x08),
        STACKED(1, 0x10),
        RETURNING(1, 0x20),
        RETURNED(1, 0x40),
        // BYTE2
        CHEATED(2, 0x01),
        REJECTED(2, 0x02),
        JAMMED(2, 0x04),
        CASSETTE_FULL(2, 0x08),
        CASSETTE_STATUS(2, 0x10),
        PAUSED(2, 0x20),
        CALIBRATION(2, 0x40),
        // BYTE3
        POWER_UP(3, 0x01),
        INVALID_COMMAND(3, 0x02),
        FAILURE(3, 0x04),
        NOTE_VALUE(3, 0x08 + 0x10 + 0x20),
        // BYTE4
        NO_PUSH_STALLED(4, 0x01),
        FLASH_DOWNLOAD_MODE(4, 0x02),
        PRE_STACK(4, 0x04),
        // BYTE5
        MODEL(5, 0xFF),
        // BYTE6
        REVISION(6, 0xFF),;

        private final int byteNum;
        private final int mask;

        private MEI_DBDS_RESPONSE_BYTE_DESC(int byteNum, int mask) {
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

    final byte[] data;

    public MeiEbdsAcceptorMsg(byte[] data) {
        this.data = data;
    }

    private static final Map<Integer, MessageType> msgTypeMap = new HashMap<Integer, MessageType>();

    static {
        for (MessageType mt : MessageType.values()) {
            msgTypeMap.put(mt.getId(), mt);
        }
    }

    public MessageType getMessageType() {
        int d = MEI_DBDS_RESPONSE_BYTE_DESC.MESSAGE_TYPE.getValue(data);
        return msgTypeMap.get(d);
    }

    public int getAck() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.ACK.getValue(data);
    }

    public boolean isEscrowed() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.ESCROWED.isSet(data);
    }

    public boolean isReturned() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.RETURNED.isSet(data);
    }

    public boolean isStacked() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.STACKED.isSet(data);
    }

    public boolean isCheated() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.CHEATED.isSet(data);
    }

    public boolean isRejected() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.REJECTED.isSet(data);
    }

    public boolean isJammed() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.JAMMED.isSet(data);
    }

    public boolean isCassetteFull() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.CASSETTE_FULL.isSet(data);
    }

    public boolean isCassetteRemoved() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.CASSETTE_STATUS.isSet(data);
    }

    public boolean isPaused() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.PAUSED.isSet(data);
    }

    public boolean isCalibration() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.CALIBRATION.isSet(data);
    }

    public boolean isPowerUp() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.POWER_UP.isSet(data);
    }

    public boolean isInvalidCommand() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.INVALID_COMMAND.isSet(data);
    }

    public boolean isFailure() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.FAILURE.isSet(data);
    }

    public int getNoteValue() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.NOTE_VALUE.getValue(data) >> 3;
    }

    public boolean isNoPushStalled() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.NO_PUSH_STALLED.isSet(data);
    }

    public boolean isFlashDownloadMode() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.FLASH_DOWNLOAD_MODE.isSet(data);
    }

    public boolean isPreStack() {
        return MEI_DBDS_RESPONSE_BYTE_DESC.PRE_STACK.isSet(data);
    }

}
