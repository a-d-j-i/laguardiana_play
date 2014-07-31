package devices.glory.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import play.Logger;

/**
 * *
 * This class correspond really to the view, but it is here for historical
 * reasons.
 *
 * @author adji
 */
public class GloryDE50ResponseWithData extends GloryDE50Response {

    static final HashMap< Byte, String> commands = new HashMap< Byte, String>();

    static {
        commands.put((byte) 0x30, "Reserved");
        commands.put((byte) 0x31, "Mode Specification");
        commands.put((byte) 0x32, "Batch Data Transmission");
        commands.put((byte) 0x33, "Counting Stop");
        commands.put((byte) 0x34, "Storing Start");
        commands.put((byte) 0x35, "Escrow Open");
        commands.put((byte) 0x36, "Escrow Close");
        commands.put((byte) 0x37, "Remote Cancelled");
        commands.put((byte) 0x38, "Device Reset (Recovering from Errors)");
        commands.put((byte) 0x39, "Currency Switching");
        commands.put((byte) 0x40, "Sense");
        commands.put((byte) 0x41, "Counting Data Request");
        commands.put((byte) 0x42, "Amount Request");
        commands.put((byte) 0x43, "Device Setting Data Request");
        commands.put((byte) 0x44, "Denomination Data Request");
        commands.put((byte) 0x45, "Log Data Request");
        commands.put((byte) 0x46, "Device Setting Data Load");
        commands.put((byte) 0x47, "Start Download");
        commands.put((byte) 0x48, "End Download");
        commands.put((byte) 0x49, "Request Download");
        commands.put((byte) 0x50, "Program Update");
        commands.put((byte) 0x51, "Set Time");
        commands.put((byte) 0x52, "Start Upload");
        commands.put((byte) 0x53, "End Upload");
        commands.put((byte) 0x54, "Get File Information By File Name");
    }

    enum GLORY_DE50_SR_DATA_DESC {

        SR1(1, 0xFF),
        SR2(2, 0xFF),
        SR3(3, 0xFF),
        SR4(4, 0xFF),
        // SR1
        OPERATING_STATE(1, 0x3F),
        // SR2
        COUNT_END(2, 0x01),
        ABNORMAL_END(2, 0x02),
        BATCH_END(2, 0x04),
        RESTORATION_END(2, 0x08),
        STORE_END(2, 0x10),
        COLLECTION_END(2, 0x20),
        // SR3
        HOPPER_BILL_PRESENT(3, 0x01),
        ESCROW_BILL_PRESENT(3, 0x02),
        REJECT_BILL_PRESENT(3, 0x04),
        DISCHARGE_FAILURE(3, 0x08),
        ESCROW_FULL(3, 0x10),
        REJECT_FULL(3, 0x20),
        // SR4
        JAMMING(4, 0x01),
        COUNTING_ERROR(4, 0x02),
        ABNORMAL_DEVICE(4, 0x04),
        ABNORMAL_STORAGE(4, 0x08);
        private final int srNum;
        private final int mask;

        private GLORY_DE50_SR_DATA_DESC(int srNum, int mask) {
            this.srNum = srNum;
            this.mask = mask;
        }

        private byte getValue(byte[] data, int payloadOffset) {
            return (byte) ((data[ srNum + payloadOffset - 1] & mask));
        }

        private boolean isSet(byte[] data, int payloadOffset) {
            return ((data[ srNum + payloadOffset - 1] & mask) != 0);
        }

        private boolean isCleared(byte[] data, int payloadOffset) {
            return ((data[ srNum + payloadOffset - 1] & mask) == 0);
        }

    }

    enum GLORY_DE50_D_DATA_DESC {

        D1(1, 0xFF),
        D2(2, 0xFF),
        D3(3, 0xFF),
        D4(4, 0xFF),
        D5(5, 0xFF),
        D6(6, 0xFF),
        D7(7, 0xFF),
        D8(8, 0xFF),
        D9(9, 0xFF),
        D10(10, 0xFF),
        D11(11, 0xFF),
        D12(12, 0xFF),
        // D1
        MODE(1, 0x3F),
        // D2
        CASSETTE_FULL_COUNTER(2, 0x04),
        CASSETTE_FULL_SENSOR(2, 0x08),
        ESCROW_SHUTTER(2, 0x10),
        ESCROW_DOOR(2, 0x20),
        // D3
        CURRENCY_SELECTED(3, 0x07),
        // D4
        MANUAL_DEPOSIT_NUMBER(4, 0x3F),
        // D5
        ERROR_CODE_OUTLINE_UPPER(5, 0xF),
        // D6
        ERROR_CODE_OUTLINE_LOWER(6, 0xF),
        // D7
        ERROR_CODE_DETAIL_UPPER(7, 0xF),
        // D8
        ERROR_CODE_DETAIL_LOWER(8, 0xF);
        private final int dNum;
        private final int mask;

        private GLORY_DE50_D_DATA_DESC(int dNum, int mask) {
            this.dNum = dNum;
            this.mask = mask;
        }

        private byte getValue(byte[] data, int payloadOffset) {
            return (byte) ((data[ dNum + payloadOffset - 1] & mask));
        }

        private boolean isSet(byte[] data, int payloadOffset) {
            return ((data[ dNum + payloadOffset - 1] & mask) != 0);
        }

        private boolean isCleared(byte[] data, int payloadOffset) {
            return ((data[ dNum + payloadOffset - 1] & mask) == 0);
        }

    }

//    public void putInBills(int slot, Integer value) {
//        bills.put(slot, value);
//    }
//
    public enum D1Mode {

        unknown(0xff), neutral(0), initial(1), deposit(2), manual(3), normal_error_recovery_mode(4), storing_error_recovery_mode(5), collect_mode(6);
        private final byte m;

        D1Mode(int m) {
            this.m = (byte) m;
        }
        static final HashMap< Byte, D1Mode> reverse = new HashMap< Byte, D1Mode>();

        static {
            byte i = 0;
            for (D1Mode s : D1Mode.values()) {
                reverse.put(s.getByte(), s);
                i++;
            }
        }

        static public D1Mode getMode(int b) {
            if (!reverse.containsKey((byte) b)) {
                Logger.error("%d invalid d1 mode", b);
                return unknown;
            }
            return reverse.get((byte) b);
        }

        public byte getByte() {
            return this.m;
        }
    }

    public enum SR1Mode {

        unknown(0xff), waiting(0), counting(1), counting_start_request(2), abnormal_device(3), being_reset(4),
        being_store(5), being_restoration(6), being_exchange_the_cassette(7), storing_start_request(8),
        being_recover_from_storing_error(9), escrow_open_request(10), escrow_close_request(11), escrow_open(12),
        escrow_close(13), initialize_start_request(14), begin_initialize(15), being_set(16), local_operation(17),
        storing_error(18), waiting_for_an_envelope_to_set(19);
        private final byte m;

        SR1Mode(int m) {
            this.m = (byte) m;
        }
        static final HashMap< Byte, SR1Mode> reverse = new HashMap< Byte, SR1Mode>();

        static {
            byte i = 0;
            for (SR1Mode s : SR1Mode.values()) {
                reverse.put(s.getByte(), s);
                i++;
            }
        }

        static public SR1Mode getMode(int b) {
            if (!reverse.containsKey((byte) b)) {
                Logger.error("%d invalid sr1 mode", b);
                return unknown;
            }
            return reverse.get((byte) b);
        }

        public byte getByte() {
            return this.m;
        }
    }

    protected final byte[] data;
    protected final int srPos;
    protected final int dPos;

    public GloryDE50ResponseWithData(byte[] data) {
        this.data = data;
        srPos = 3;
        dPos = data.length - 14;
    }

    public SR1Mode getSr1Mode() {
        return SR1Mode.getMode(GLORY_DE50_SR_DATA_DESC.OPERATING_STATE.getValue(data, srPos));
    }

    public D1Mode getD1Mode() {
        return D1Mode.getMode(GLORY_DE50_D_DATA_DESC.MODE.getValue(data, dPos));
    }

    public boolean isCollectionEnd() {
        return GLORY_DE50_SR_DATA_DESC.COLLECTION_END.isSet(data, srPos);
    }

    public boolean isStoreEnd() {
        return GLORY_DE50_SR_DATA_DESC.STORE_END.isSet(data, srPos);
    }

    public boolean isRestorationEnd() {
        return GLORY_DE50_SR_DATA_DESC.RESTORATION_END.isSet(data, srPos);
    }

    public boolean isBatchEnd() {
        return GLORY_DE50_SR_DATA_DESC.BATCH_END.isSet(data, srPos);
    }

    public boolean isAbnoramalEnd() {
        return GLORY_DE50_SR_DATA_DESC.ABNORMAL_END.isSet(data, srPos);
    }

    public boolean isCountEnd() {
        return GLORY_DE50_SR_DATA_DESC.COUNT_END.isSet(data, srPos);
    }

    public boolean isRejectFull() {
        return GLORY_DE50_SR_DATA_DESC.REJECT_FULL.isSet(data, srPos);
    }

    public boolean isEscrowFull() {
        return GLORY_DE50_SR_DATA_DESC.ESCROW_FULL.isSet(data, srPos);
    }

    public boolean isDischargingFailure() {
        return GLORY_DE50_SR_DATA_DESC.DISCHARGE_FAILURE.isSet(data, srPos);
    }

    public boolean isRejectBillPresent() {
        return GLORY_DE50_SR_DATA_DESC.REJECT_BILL_PRESENT.isSet(data, srPos);
    }

    public boolean isEscrowBillPresent() {
        return GLORY_DE50_SR_DATA_DESC.ESCROW_BILL_PRESENT.isSet(data, srPos);
    }

    public boolean isHopperBillPresent() {
        return GLORY_DE50_SR_DATA_DESC.HOPPER_BILL_PRESENT.isSet(data, srPos);
    }

    public boolean isAbnormalStorage() {
        return GLORY_DE50_SR_DATA_DESC.ABNORMAL_STORAGE.isSet(data, srPos);
    }

    public boolean isAbnormalDevice() {
        return GLORY_DE50_SR_DATA_DESC.ABNORMAL_DEVICE.isSet(data, srPos);
    }

    public boolean isCountingError() {
        return GLORY_DE50_SR_DATA_DESC.COUNTING_ERROR.isSet(data, srPos);
    }

    public boolean isJamming() {
        return GLORY_DE50_SR_DATA_DESC.JAMMING.isSet(data, srPos);
    }

    public boolean isDoorEscrow() {
        return GLORY_DE50_D_DATA_DESC.ESCROW_DOOR.isSet(data, dPos);
    }

    public boolean isDoorEscrowShutter() {
        return GLORY_DE50_D_DATA_DESC.ESCROW_SHUTTER.isSet(data, dPos);
    }

    public boolean isCassetteFullSensor() {
        return GLORY_DE50_D_DATA_DESC.CASSETTE_FULL_SENSOR.isSet(data, dPos);
    }

    public boolean isCassetteFullCounter() {
        return GLORY_DE50_D_DATA_DESC.CASSETTE_FULL_COUNTER.isSet(data, dPos);
    }

    public int getCassete() {
        return 0;
    }

    public int getCurrency() {
        return GLORY_DE50_D_DATA_DESC.CURRENCY_SELECTED.getValue(data, dPos);
    }

    public int getManualDepositNumber() {
        return GLORY_DE50_D_DATA_DESC.MANUAL_DEPOSIT_NUMBER.getValue(data, dPos);
    }

    public int getErrorCode() {
        byte n1 = GLORY_DE50_D_DATA_DESC.ERROR_CODE_OUTLINE_UPPER.getValue(data, dPos);
        byte n2 = GLORY_DE50_D_DATA_DESC.ERROR_CODE_OUTLINE_LOWER.getValue(data, dPos);
        byte n3 = GLORY_DE50_D_DATA_DESC.ERROR_CODE_DETAIL_UPPER.getValue(data, dPos);
        byte n4 = GLORY_DE50_D_DATA_DESC.ERROR_CODE_DETAIL_LOWER.getValue(data, dPos);
        return (n4 | n3 << 4 | n2 << 8 | n1 << 12);
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean hasData() {
        return true;
    }

    public String getErrorCodeRepr() {
        return String.format("0x%04X", getErrorCode());
    }

    public String getDataRepr() {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("0x%02x ", b & 0xff));
        }
        sb.append(" - ");
        for (byte b : data) {
            sb.append(String.format("%02d ", b & 0xff));
        }
        sb.append(" - ");
        for (byte b : data) {
            if (b > 27 && b < 127) {
                sb.append(String.format("%c ", (char) b));
            } else {
                sb.append(String.format("- ", b & 0xff));
            }
        }
        return sb.toString();
    }

    public Map<String, String> getDRepr() {
        Map<String, String> ret = new TreeMap<String, String>();
        for (GLORY_DE50_D_DATA_DESC a : GLORY_DE50_D_DATA_DESC.values()) {
            byte b = a.getValue(data, dPos);
            ret.put(a.name(), String.format("0x%02x %02d", b, b));
        }
        return ret;
    }

    public Map<String, String> getSRRepr() {
        Map<String, String> ret = new TreeMap<String, String>();
        for (GLORY_DE50_SR_DATA_DESC a : GLORY_DE50_SR_DATA_DESC.values()) {
            byte b = a.getValue(data, srPos);
            ret.put(a.name(), String.format("0x%02x %02d", b, b));
        }
        return ret;
    }

    static public class Denomination {

        public int idx;
        public String currencyCode;
        public boolean newVal;
        public Integer denominationCode;
        public Integer value;
    }

    public List<Denomination> denominationData = null;

    public List<Denomination> getDenominationData() {
        if (denominationData != null) {
            return denominationData;
        }
        if (data.length != 21 + 64 * 10) {
            Logger.error(String.format("Command don't have denominations, invalid length %d expected", data.length));
            return denominationData;
        }

        denominationData = new ArrayList<Denomination>();
        for (int i = 7; i < data.length; i += 10) {
            if (i + 21 <= data.length) {
                Denomination d = new Denomination();
                d.idx = (i - 7) / 10;
                byte[] b = {data[i], data[i + 1], data[i + 2]};
                d.currencyCode = new String(b);
                d.newVal = (data[ i + 3] != 0x30);
                // TODO: Check for 0x4X
                d.denominationCode = 32 - (data[ i + 4] & 0x0F);

                Double dd = (Math.pow(10, getDecDigit(data[i + 6])) * (getDecDigit(data[i + 7]) * 100 + getDecDigit(data[i + 8]) * 10 + getDecDigit(data[i + 9]) * 1));
                d.value = dd.intValue();
                if (data[ i + 5] != 0x30) {
                    d.value = -d.value;
                }
                denominationData.add(d);
            }
        }
        return denominationData;
    }

    Map<Integer, Integer> bills = null;

    public Map<Integer, Integer> getBills() {
        if (bills != null) {
            return bills;
        }
        if (data.length == 32 * 3 + 21) {
            bills = new HashMap<Integer, Integer>();
            for (int slot = 0; slot < 32; slot++) {
                Integer value = getDecDigit(data[ 3 * slot + 7]) * 100
                        + getDecDigit(data[ 3 * slot + 8]) * 10
                        + getDecDigit(data[ 3 * slot + 9]);
                bills.put(slot, value);
                //Logger.debug(String.format("Bill %d: quantity %d", slot, value));
            }
        } else if (data.length == 65 * 4 + 21) {
            bills = new HashMap<Integer, Integer>();
            for (int slot = 0; slot < 65; slot++) {
                Integer value = getHexDigit(data[ 4 * slot + 7]) * 1000
                        + getHexDigit(data[ 4 * slot + 8]) * 100
                        + getHexDigit(data[ 4 * slot + 9]) * 10
                        + getHexDigit(data[ 4 * slot + 10]);
                bills.put(slot, value);
                //Logger.debug(String.format("Bill %d: quantity %d", slot, value));
            }
        } else {
            Logger.error("Invalid response length %d expected", data.length);
        }
        return bills;
    }

    int fileSize = -1;

    public int getFileSize() {
        if (fileSize >= 0) {
            return fileSize;
        }
        parseData();
        return fileSize;
    }
    Date date = null;

    public Date getDate() {
        if (date != null) {
            return date;
        }
        parseData();
        return date;
    }

    protected void parseData() {
        byte[] b = data;
        int l = 0;
        if (data.length == 16 + 21) {

            int i;
            for (i = 7; i < 8; i++) {
                if (b[i] >= 0x30 && b[i] <= 0x3F) {
                    l += getHexDigit(b[i]) * Math.pow(16, 8 - i - 1);
                } else {
                    Logger.error(String.format("Invalid digit %d == 0x%x", b[i], b[i]));
                    return;
                }
            }
            fileSize = l;
            int year = getDecDigit(b[i++]) * 1000 + getDecDigit(b[i++]) * 100 + getDecDigit(b[i++]) * 10 + getDecDigit(b[i++]) * 1;
            int month = getDecDigit(b[i++]) * 10 + getDecDigit(b[i++]) * 1;
            int day = getDecDigit(b[i++]) * 10 + getDecDigit(b[i++]) * 1;
            GregorianCalendar g = new GregorianCalendar(year, month, day);
            date = g.getTime();
        } else if (data.length == 8 + 21) {
            for (int i = 7; i < b.length; i++) {
                if (b[i] >= 0x30 && b[i] <= 0x3F) {
                    l += getHexDigit(b[i]) * Math.pow(16, b.length - i - 1);
                } else {
                    Logger.error("Invalid digit %d == 0x%x", b[i], b[i]);
                    return;
                }
            }
            fileSize = l;

        } else {
            Logger.error(String.format("Invalid response length %d expected 8 / 16 bytes hex number", data.length));
        }
    }

    @Override
    public String toString() {
//        return "GloryDE50ResponseWithData{" + "data=" + getDataRepr() + ", srPos=" + srPos + ", dPos=" + dPos + '}';
        return "GloryDE50ResponseWithData{ SR1Mode : " + getSr1Mode().toString() + " D1Mode : " + getD1Mode().toString() + " }";
    }

    protected byte getDecDigit(byte l) {
        if (l >= 0x30 && l <= 0x39) {
            return (byte) (l - 0x30);
        }
        throw new NumberFormatException(String.format("invalid digit 0x%x", l));

    }

    protected byte getHexDigit(byte l) {
        if (l >= 0x30 && l <= 0x3F) {
            return (byte) (l - 0x30);
        }
        throw new NumberFormatException(String.format("invalid digit 0x%x", l));
    }
}
