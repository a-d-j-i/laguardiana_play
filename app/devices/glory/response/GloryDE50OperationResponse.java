package devices.glory.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * *
 * This class correspond really to the view, but it is here for historical
 * reasons.
 *
 * @author adji
 */
public class GloryDE50OperationResponse implements Serializable {

    static public class Denomination {

        public int idx;
        public String currencyCode;
        public boolean newVal;
        public Integer denominationCode;
        public Integer value;
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

    public void setBill(int slot, Integer value) {
        this.bills.put(slot, value);
    }

    @Override
    public String toString() {
        return "GloryDE50OperationResponse{" + "sr1Mode=" + sr1Mode + ", d1Mode=" + d1Mode + ", collectionEnd=" + collectionEnd + ", storeEnd=" + storeEnd + ", restorationEnd=" + restorationEnd + ", batchEnd=" + batchEnd + ", abnoramalEnd=" + abnoramalEnd + ", countEnd=" + countEnd + ", rejectFull=" + rejectFull + ", escrowFull=" + escrowFull + ", dischargingFailure=" + dischargingFailure + ", rejectBillPresent=" + rejectBillPresent + ", escrowBillPresent=" + escrowBillPresent + ", hopperBillPresent=" + hopperBillPresent + ", abnormalStorage=" + abnormalStorage + ", abnormalDevice=" + abnormalDevice + ", countingError=" + countingError + ", jamming=" + jamming + ", doorEscrow=" + doorEscrow + ", doorEscrowShutter=" + doorEscrowShutter + ", cassetteFullSensor=" + cassetteFullSensor + ", cassetteFullCounter=" + cassetteFullCounter + ", cassete=" + cassete + ", currency=" + currency + ", manualDepositNumber=" + manualDepositNumber + ", codeOutline=" + codeOutline + ", codeDetail=" + codeDetail + ", d9=" + d9 + ", d10=" + d10 + ", d11=" + d11 + ", d12=" + d12 + ", sr2=" + sr2 + ", sr3=" + sr3 + ", sr4=" + sr4 + ", d2=" + d2 + ", d3=" + d3 + ", d4=" + d4 + ", d5=" + d5 + ", d6=" + d6 + ", d7=" + d7 + ", d8=" + d8 + ", data=" + Arrays.toString(data) + ", bills=" + bills + '}';
    }

    final public List<Denomination> denominationData = new ArrayList<Denomination>();

    public List<Denomination> getDenominationData() {
        return denominationData;
    }

    public void addToDenominationData(Denomination d) {
        denominationData.add(d);
    }

    public GloryDE50OperationResponseParser getRepr() {
        return new GloryDE50OperationResponseParser(this);
    }
    int fileSize = -1;

    public void setFileSize(int l) {
        fileSize = l;
    }

    public int getFileSize() {
        return fileSize;
    }
    Date date;

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

}
