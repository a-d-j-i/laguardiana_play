package devices.glory;

import devices.glory.command.CommandWithAckResponse;
import devices.glory.command.CommandWithCountingDataResponse;
import devices.glory.command.CommandWithDataResponse;
import devices.glory.command.GloryCommandAbstract;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * *
 * This class correspond really to the view, but it is here for historical
 * reasons.
 *
 * @author adji
 */
public class GloryState implements Serializable {

    public enum D1Mode {

        unknown( 0xff ), neutral( 0 ), initial( 1 ), deposit( 2 ), manual( 3 ), normal_error_recovery_mode( 4 ), storing_error_recovery_mode( 5 ), collect_mode( 6 );
        private final byte m;

        D1Mode( int m ) {
            this.m = ( byte ) m;
        }
        static final HashMap< Byte, D1Mode> reverse = new HashMap< Byte, D1Mode>();

        static {
            byte i = 0;
            for ( D1Mode s : D1Mode.values() ) {
                reverse.put( s.getByte(), s );
                i++;
            }
        }

        static public D1Mode getMode( int b ) {
            if ( !reverse.containsKey( ( byte ) b ) ) {
                return unknown;
            }
            return reverse.get( ( byte ) b );
        }

        public byte getByte() {
            return this.m;
        }
    }

    public enum SR1Mode {

        unknown( 0xff ), waiting( 0 ), counting( 1 ), counting_start_request( 2 ), abnormal_device( 3 ), being_reset( 4 ),
        being_store( 5 ), being_restoration( 6 ), being_exchange_the_cassette( 7 ), storing_start_request( 8 ),
        being_recover_from_storing_error( 9 ), escrow_open_request( 10 ), escrow_close_request( 11 ), escrow_open( 12 ),
        escrow_close( 13 ), initialize_start_request( 14 ), begin_initialize( 15 ), being_set( 16 ), local_operation( 17 ),
        storing_error( 18 ), waiting_for_an_envelope_to_set( 19 );
        private final byte m;

        SR1Mode( int m ) {
            this.m = ( byte ) m;
        }
        static final HashMap< Byte, SR1Mode> reverse = new HashMap< Byte, SR1Mode>();

        static {
            byte i = 0;
            for ( SR1Mode s : SR1Mode.values() ) {
                reverse.put( s.getByte(), s );
                i++;
            }
        }

        static public SR1Mode getMode( int b ) {
            if ( !reverse.containsKey( ( byte ) b ) ) {
                return unknown;
            }
            return reverse.get( ( byte ) b );
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
    Map<Integer, Integer> bills = new HashMap<Integer, Integer>();
    String lastError = null;

    public boolean setStatusOk( GloryCommandAbstract response ) {
        if ( response == null ) {
            lastError = "invalid response";
            return false;
        }
        if ( response.getError() != null && !response.getError().isEmpty() ) {
            lastError = response.getError();
            return false;
        }
        lastError = null;
        if ( response instanceof CommandWithCountingDataResponse ) {
            CommandWithCountingDataResponse cmd = ( CommandWithCountingDataResponse ) response;
            bills = cmd.getBills();
        }
        if ( response instanceof CommandWithDataResponse ) {
            CommandWithDataResponse cmd = ( CommandWithDataResponse ) response;
            sr1Mode = cmd.getSr1();
            d1Mode = cmd.getD1();

            byte a = cmd.getSr2();
            collectionEnd = ( a & 0x20 ) != 0;
            storeEnd = ( a & 0x10 ) != 0;
            restorationEnd = ( a & 0x08 ) != 0;
            batchEnd = ( a & 0x04 ) != 0;
            abnoramalEnd = ( a & 0x02 ) != 0;
            countEnd = ( a & 0x01 ) != 0;


            a = cmd.getSr3();
            rejectFull = ( a & 0x20 ) != 0;
            escrowFull = ( a & 0x10 ) != 0;
            dischargingFailure = ( a & 0x08 ) != 0;
            rejectBillPresent = ( a & 0x04 ) != 0;
            escrowBillPresent = ( a & 0x02 ) != 0;
            hopperBillPresent = ( a & 0x01 ) != 0;


            a = cmd.getSr4();
            abnormalStorage = ( a & 0x08 ) != 0;
            abnormalDevice = ( a & 0x04 ) != 0;
            countingError = ( a & 0x02 ) != 0;
            jamming = ( a & 0x01 ) != 0;

            a = cmd.getD2();
            doorEscrow = ( a & 0x20 ) != 0;
            doorEscrowShutter = ( a & 0x10 ) != 0;
            cassetteFullSensor = ( a & 0x08 ) != 0;
            cassetteFullCounter = ( a & 0x04 ) != 0;


            cassete = cmd.getD2() & 7;
            currency = cmd.getD3();
            manualDepositNumber = cmd.getD4();
            codeOutline = cmd.getD5() << 8 + cmd.getD6();
            codeDetail = cmd.getD7() << 8 + cmd.getD8();
            d9 = cmd.getD9();
            d10 = cmd.getD10();
            d11 = cmd.getD11();
            d12 = cmd.getD12();
        }
        if ( response instanceof CommandWithAckResponse ) {
        }
        return true;
    }

    public String getLastError() {
        return lastError;
    }

    public boolean isError() {
        return lastError != null;
    }

    public boolean isAbnoramalEnd() {
        return abnoramalEnd;
    }

    public boolean isAbnormalDevice() {
        return abnormalDevice;
    }

    public boolean isAbnormalStorage() {
        return abnormalStorage;
    }

    public boolean isBatchEnd() {
        return batchEnd;
    }

    public Map<Integer, Integer> getBills() {
        return bills;
    }

    public int getCassete() {
        return cassete;
    }

    public boolean isCassetteFullCounter() {
        return cassetteFullCounter;
    }

    public boolean isCassetteFullSensor() {
        return cassetteFullSensor;
    }

    public int getCodeDetail() {
        return codeDetail;
    }

    public int getCodeOutline() {
        return codeOutline;
    }

    public boolean isCollectionEnd() {
        return collectionEnd;
    }

    public boolean isCountEnd() {
        return countEnd;
    }

    public boolean isCountingError() {
        return countingError;
    }

    public int getCurrency() {
        return currency;
    }

    public int getD10() {
        return d10;
    }

    public int getD11() {
        return d11;
    }

    public int getD12() {
        return d12;
    }

    public D1Mode getD1Mode() {
        return d1Mode;
    }

    public int getD9() {
        return d9;
    }

    public boolean isDischargingFailure() {
        return dischargingFailure;
    }

    public boolean isDoorEscrow() {
        return doorEscrow;
    }

    public boolean isDoorEscrowShutter() {
        return doorEscrowShutter;
    }

    public boolean isEscrowBillPresent() {
        return escrowBillPresent;
    }

    public boolean isEscrowFull() {
        return escrowFull;
    }

    public boolean isHopperBillPresent() {
        return hopperBillPresent;
    }

    public boolean isJamming() {
        return jamming;
    }

    public int getManualDepositNumber() {
        return manualDepositNumber;
    }

    public boolean isRejectBillPresent() {
        return rejectBillPresent;
    }

    public boolean isRejectFull() {
        return rejectFull;
    }

    public boolean isRestorationEnd() {
        return restorationEnd;
    }

    public SR1Mode getSr1Mode() {
        return sr1Mode;
    }

    public boolean isStoreEnd() {
        return storeEnd;
    }
}
