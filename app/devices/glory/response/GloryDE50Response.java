package devices.glory.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * *
 * This class correspond really to the view, but it is here for historical
 * reasons.
 *
 * @author adji
 */
public class GloryDE50Response implements Serializable {

    static public class Denomination {

        public int idx;
        public String currencyCode;
        public boolean newVal;
        public Integer denominationCode;
        public Integer value;
    }

    public GloryDE50Response(String error) {
        this.error = error;
    }

    public GloryDE50Response() {
    }

    public void putInBills(int slot, Integer value) {
        bills.put(slot, value);
    }

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
                return unknown;
            }
            return reverse.get((byte) b);
        }

        public byte getByte() {
            return this.m;
        }
    }

    SR1Mode sr1Mode = SR1Mode.unknown;
    D1Mode d1Mode = D1Mode.unknown;
    boolean collectionEnd;
    boolean storeEnd;
    boolean restorationEnd;
    boolean batchEnd;
    boolean abnoramalEnd;
    boolean countEnd;
    boolean rejectFull;
    boolean escrowFull;
    boolean dischargingFailure;
    boolean rejectBillPresent;
    boolean escrowBillPresent;
    boolean hopperBillPresent;
    boolean abnormalStorage;
    boolean abnormalDevice;
    boolean countingError;
    boolean jamming;
    boolean doorEscrow;
    boolean doorEscrowShutter;
    boolean cassetteFullSensor;
    boolean cassetteFullCounter;
    int cassete;
    int currency;
    int manualDepositNumber;
    int codeOutline;
    int codeDetail;
    int d9;
    int d10;
    int d11;
    int d12;
    byte sr2;
    byte sr3;
    byte sr4;
    byte d2;
    byte d3;
    byte d4;
    byte d5;
    byte d6;
    byte d7;
    byte d8;

    byte[] data = null;
    Map<Integer, Integer> bills = new HashMap<Integer, Integer>();
    String error = null;

    public SR1Mode getSr1Mode() {
        return sr1Mode;
    }

    public void setSr1Mode(SR1Mode sr1Mode) {
        this.sr1Mode = sr1Mode;
    }

    public D1Mode getD1Mode() {
        return d1Mode;
    }

    public void setD1Mode(D1Mode d1Mode) {
        this.d1Mode = d1Mode;
    }

    public boolean isCollectionEnd() {
        return collectionEnd;
    }

    public void setCollectionEnd(boolean collectionEnd) {
        this.collectionEnd = collectionEnd;
    }

    public boolean isStoreEnd() {
        return storeEnd;
    }

    public void setStoreEnd(boolean storeEnd) {
        this.storeEnd = storeEnd;
    }

    public boolean isRestorationEnd() {
        return restorationEnd;
    }

    public void setRestorationEnd(boolean restorationEnd) {
        this.restorationEnd = restorationEnd;
    }

    public boolean isBatchEnd() {
        return batchEnd;
    }

    public void setBatchEnd(boolean batchEnd) {
        this.batchEnd = batchEnd;
    }

    public boolean isAbnoramalEnd() {
        return abnoramalEnd;
    }

    public void setAbnoramalEnd(boolean abnoramalEnd) {
        this.abnoramalEnd = abnoramalEnd;
    }

    public boolean isCountEnd() {
        return countEnd;
    }

    public void setCountEnd(boolean countEnd) {
        this.countEnd = countEnd;
    }

    public boolean isRejectFull() {
        return rejectFull;
    }

    public void setRejectFull(boolean rejectFull) {
        this.rejectFull = rejectFull;
    }

    public boolean isEscrowFull() {
        return escrowFull;
    }

    public void setEscrowFull(boolean escrowFull) {
        this.escrowFull = escrowFull;
    }

    public boolean isDischargingFailure() {
        return dischargingFailure;
    }

    public void setDischargingFailure(boolean dischargingFailure) {
        this.dischargingFailure = dischargingFailure;
    }

    public boolean isRejectBillPresent() {
        return rejectBillPresent;
    }

    public void setRejectBillPresent(boolean rejectBillPresent) {
        this.rejectBillPresent = rejectBillPresent;
    }

    public boolean isEscrowBillPresent() {
        return escrowBillPresent;
    }

    public void setEscrowBillPresent(boolean escrowBillPresent) {
        this.escrowBillPresent = escrowBillPresent;
    }

    public boolean isHopperBillPresent() {
        return hopperBillPresent;
    }

    public void setHopperBillPresent(boolean hopperBillPresent) {
        this.hopperBillPresent = hopperBillPresent;
    }

    public boolean isAbnormalStorage() {
        return abnormalStorage;
    }

    public void setAbnormalStorage(boolean abnormalStorage) {
        this.abnormalStorage = abnormalStorage;
    }

    public boolean isAbnormalDevice() {
        return abnormalDevice;
    }

    public void setAbnormalDevice(boolean abnormalDevice) {
        this.abnormalDevice = abnormalDevice;
    }

    public boolean isCountingError() {
        return countingError;
    }

    public void setCountingError(boolean countingError) {
        this.countingError = countingError;
    }

    public boolean isJamming() {
        return jamming;
    }

    public void setJamming(boolean jamming) {
        this.jamming = jamming;
    }

    public boolean isDoorEscrow() {
        return doorEscrow;
    }

    public void setDoorEscrow(boolean doorEscrow) {
        this.doorEscrow = doorEscrow;
    }

    public boolean isDoorEscrowShutter() {
        return doorEscrowShutter;
    }

    public void setDoorEscrowShutter(boolean doorEscrowShutter) {
        this.doorEscrowShutter = doorEscrowShutter;
    }

    public boolean isCassetteFullSensor() {
        return cassetteFullSensor;
    }

    public void setCassetteFullSensor(boolean cassetteFullSensor) {
        this.cassetteFullSensor = cassetteFullSensor;
    }

    public boolean isCassetteFullCounter() {
        return cassetteFullCounter;
    }

    public void setCassetteFullCounter(boolean cassetteFullCounter) {
        this.cassetteFullCounter = cassetteFullCounter;
    }

    public int getCassete() {
        return cassete;
    }

    public void setCassete(int cassete) {
        this.cassete = cassete;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public int getManualDepositNumber() {
        return manualDepositNumber;
    }

    public void setManualDepositNumber(int manualDepositNumber) {
        this.manualDepositNumber = manualDepositNumber;
    }

    public int getCodeOutline() {
        return codeOutline;
    }

    public void setCodeOutline(int codeOutline) {
        this.codeOutline = codeOutline;
    }

    public int getCodeDetail() {
        return codeDetail;
    }

    public void setCodeDetail(int codeDetail) {
        this.codeDetail = codeDetail;
    }

    public int getD9() {
        return d9;
    }

    public void setD9(int d9) {
        this.d9 = d9;
    }

    public int getD10() {
        return d10;
    }

    public void setD10(int d10) {
        this.d10 = d10;
    }

    public int getD11() {
        return d11;
    }

    public void setD11(int d11) {
        this.d11 = d11;
    }

    public int getD12() {
        return d12;
    }

    public void setD12(int d12) {
        this.d12 = d12;
    }

    public byte getSr2() {
        return sr2;
    }

    public void setSr2(byte sr2) {
        this.sr2 = sr2;
    }

    public byte getSr3() {
        return sr3;
    }

    public void setSr3(byte sr3) {
        this.sr3 = sr3;
    }

    public byte getSr4() {
        return sr4;
    }

    public void setSr4(byte sr4) {
        this.sr4 = sr4;
    }

    public byte getD2() {
        return d2;
    }

    public void setD2(byte d2) {
        this.d2 = d2;
    }

    public byte getD3() {
        return d3;
    }

    public void setD3(byte d3) {
        this.d3 = d3;
    }

    public byte getD4() {
        return d4;
    }

    public void setD4(byte d4) {
        this.d4 = d4;
    }

    public byte getD5() {
        return d5;
    }

    public void setD5(byte d5) {
        this.d5 = d5;
    }

    public byte getD6() {
        return d6;
    }

    public void setD6(byte d6) {
        this.d6 = d6;
    }

    public byte getD7() {
        return d7;
    }

    public void setD7(byte d7) {
        this.d7 = d7;
    }

    public byte getD8() {
        return d8;
    }

    public void setD8(byte d8) {
        this.d8 = d8;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Map<Integer, Integer> getBills() {
        return bills;
    }

    public void setBills(Map<Integer, Integer> bills) {
        this.bills = bills;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isError() {
        return error != null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // from return parser
    static HashMap< Byte, String> commands = new HashMap< Byte, String>();

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
    static HashMap< Byte, String> sr2bits = new HashMap< Byte, String>();

    static {
        sr2bits.put((byte) 0x20, "sr2 Collection End");
        sr2bits.put((byte) 0x10, "sr2 Store End");
        sr2bits.put((byte) 0x08, "sr2 Restoration End");
        sr2bits.put((byte) 0x04, "sr2 Batch End");
        sr2bits.put((byte) 0x02, "sr2 Abnormal End");
        sr2bits.put((byte) 0x01, "sr2 Count End");
    }
    static HashMap< Byte, String> sr3bits = new HashMap< Byte, String>();

    static {
        sr3bits.put((byte) 0x20, "sr3 Reject Full");
        sr3bits.put((byte) 0x10, "sr3 Escrow Full");
        sr3bits.put((byte) 0x08, "sr3 Discharging failure");
        sr3bits.put((byte) 0x04, "sr3 Rejected Bill present");
        sr3bits.put((byte) 0x02, "sr3 Escrow bill present");
        sr3bits.put((byte) 0x01, "sr3 Hopper bill present");
    }
    static HashMap< Byte, String> sr4bits = new HashMap< Byte, String>();

    static {
        sr4bits.put((byte) 0x08, "sr4 Abnormal storage");
        sr4bits.put((byte) 0x04, "sr4 Abnormal device");
        sr4bits.put((byte) 0x02, "sr4 Counting error");
        sr4bits.put((byte) 0x01, "sr4 Jamming");
    }
    static HashMap< Byte, String> d2bits = new HashMap< Byte, String>();

    static {
        d2bits.put((byte) 0x20, "d2 Escrow");
        d2bits.put((byte) 0x10, "d2 Escrow shutter");
        d2bits.put((byte) 0x08, "d2 Cassette Full (sensor)");
        d2bits.put((byte) 0x04, "d2 Cassette Full (Counter)");
    }

    public class ResponseRepr {

        public String msg = null;
        public String SR1Mode = "Invalid mode";
        public String D1Mode = "Invalid mode";
        public ArrayList<String> srBits = new ArrayList<String>();
        public ArrayList<String> d2Bits = new ArrayList<String>();
        public ArrayList<String> info = new ArrayList<String>();
        public boolean haveData = false;
        public String reprData;

        public String getErrorAsMsg() {
            String ret;
            StringBuilder b = new StringBuilder();
            if (msg != null) {
                b.append(msg);
            }
            if (error != null) {
                b.append(error);
            }
            ret = b.toString();
            if (ret.isEmpty()) {
                return null;
            }
            return ret;
        }

        public ArrayList< String> getSrBits() {
            return srBits;
        }

        public ArrayList<String> getD2Bits() {
            return d2Bits;
        }

        public ArrayList<String> getInfo() {
            return info;
        }

        public boolean haveData() {
            return haveData;
        }

    }

    private ArrayList<String> getBits(byte data, HashMap<Byte, String> bits) {
        ArrayList<String> a = new ArrayList<String>();

        for (int i = 0; i < 8; i++) {
            byte b = (byte) (1 << i);
            if ((data & b) != 0 && bits.get(b) != null) {
                a.add(String.format("%d : %s", i, bits.get(b)));
            }
        }
        return a;
    }

    public ResponseRepr repr() {
        ResponseRepr ret = new ResponseRepr();
        ret.SR1Mode = String.format("Sr1 mode 0x%x %s", getSr1Mode().getByte(), getSr1Mode().name());
        ret.D1Mode = String.format("d1 mode 0x%x %s", getD1Mode().getByte(), getD1Mode().name());
        ArrayList<String> a;

        a = getBits(getSr2(), sr2bits);
        ret.srBits.addAll(a);
        a = getBits(getSr3(), sr3bits);
        ret.srBits.addAll(a);
        a = getBits(getSr4(), sr4bits);
        ret.srBits.addAll(a);

        ret.d2Bits.addAll(getBits(getD2(), d2bits));

        ret.info.add(String.format("d2 cassete %d", getD2() & 7));
        ret.info.add(String.format("d3 currency select or country code 0x%x", getD3()));
        ret.info.add(String.format("d4 manual deposit number 0x%x", getD4()));
        ret.info.add(String.format("d5 error code outline upper 0x%x", getD5()));
        ret.info.add(String.format("d6 error code outline lower 0x%x", getD6()));
        ret.info.add(String.format("d7 error code detail upper 0x%x", getD7()));
        ret.info.add(String.format("d8 error code detail lower 0x%x", getD8()));
        ret.info.add(String.format("d9 0x%x", getD9()));
        ret.info.add(String.format("d10 0x%x", getD10()));
        ret.info.add(String.format("d11 0x%x", getD11()));
        ret.info.add(String.format("d12 0x%x", getD12()));

        ret.reprData = null;
        StringBuilder hexString = new StringBuilder();
        if (getData() != null) {
            hexString.append(new String(getData()));
            hexString.append("\n---------------\n");
            for (byte b : getData()) {
                hexString.append(" ");
                hexString.append(Integer.toHexString(0xFF & b));
            }
            ret.reprData = hexString.toString();
        }
        return ret;
    }

    public ArrayList<Denomination> denominationData = null;

    public ArrayList<Denomination> getDenominationData() {
        return denominationData;
    }

    public void addToDenominationData(Denomination d) {
        denominationData.add(d);
    }

}
