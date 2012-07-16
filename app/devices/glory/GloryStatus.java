package devices.glory;

import devices.glory.command.CommandAbstract;
import devices.glory.command.CommandWithCountingDataResponse;
import devices.glory.command.CommandWithDataResponse;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class GloryStatus implements Serializable {

    enum GloryMode {

        unknown( 0 ), waiting( 1 ), counting( 2 ), counting_start_request( 3 ), abnormal_device( 4 ), being_reset( 5 ), being_store(
        6 ), being_restoration( 7 ), being_exchange_the_cassette( 8 ), storing_start_request( 9 ), being_recover_from_storing_error(
        10 ), escrow_open_request( 11 ), escrow_close_request( 12 ), escrow_open( 13 ), escrow_close(
        14 ), initialize_start_request( 15 ), begin_initialize( 16 ), being_set( 17 ), local_operation(
        18 ), storing_error( 19 ), waiting_for_an_envelope_to_set( 20 );
        private final byte m;

        GloryMode( int m ) {
            this.m = ( byte ) m;
        }

        GloryMode( CommandWithDataResponse response ) {
            if ( response.getSr1() > 19 || response.getSr1() < 0 ) {
                this.m = 0;
            } else {
                this.m = ( byte ) ( response.getSr1() + 1 );
            }
        }

        byte getMode() {
            return this.m;
        }
    }
    static HashMap< Byte, String> commands = new HashMap< Byte, String>();

    static {
        commands.put( ( byte ) ( byte ) 0x30, "Reserved" );
        commands.put( ( byte ) ( byte ) 0x31, "Mode Specification" );
        commands.put( ( byte ) ( byte ) 0x32, "Batch Data Transmission" );
        commands.put( ( byte ) ( byte ) 0x33, "Counting Stop" );
        commands.put( ( byte ) 0x34, "Storing Start" );
        commands.put( ( byte ) 0x35, "Escrow Open" );
        commands.put( ( byte ) 0x36, "Escrow Close" );
        commands.put( ( byte ) 0x37, "Remote Cancelled" );
        commands.put( ( byte ) 0x38, "Device Reset (Recovering from Errors)" );
        commands.put( ( byte ) 0x39, "Currency Switching" );
        commands.put( ( byte ) 0x40, "Sense" );
        commands.put( ( byte ) 0x41, "Counting Data Request" );
        commands.put( ( byte ) 0x42, "Amount Request" );
        commands.put( ( byte ) 0x43, "Device Setting Data Request" );
        commands.put( ( byte ) 0x44, "Denomination Data Request" );
        commands.put( ( byte ) 0x45, "Log Data Request" );
        commands.put( ( byte ) 0x46, "Device Setting Data Load" );
        commands.put( ( byte ) 0x47, "Start Download" );
        commands.put( ( byte ) 0x48, "End Download" );
        commands.put( ( byte ) 0x49, "Request Download" );
        commands.put( ( byte ) 0x50, "Program Update" );
        commands.put( ( byte ) 0x51, "Set Time" );
        commands.put( ( byte ) 0x52, "Start Upload" );
        commands.put( ( byte ) 0x53, "End Upload" );
        commands.put( ( byte ) 0x54, "Get File Information By File Name" );
    }
    static HashMap< Byte, String> sr1Modes = new HashMap< Byte, String>();

    static {
        sr1Modes.put( ( byte ) 0, "Waiting" );
        sr1Modes.put( ( byte ) 1, "Counting" );
        sr1Modes.put( ( byte ) 2, "Counting start request" );
        sr1Modes.put( ( byte ) 3, "Abnormal device" );
        sr1Modes.put( ( byte ) 4, "Being reset" );
        sr1Modes.put( ( byte ) 5, "Being store" );
        sr1Modes.put( ( byte ) 6, "Being restoration" );
        sr1Modes.put( ( byte ) 7, "Being exchange the cassette" );
        sr1Modes.put( ( byte ) 8, "Storing start request" );
        sr1Modes.put( ( byte ) 9, "Being recover from Storing error" );
        sr1Modes.put( ( byte ) 10, "Escrow open request" );
        sr1Modes.put( ( byte ) 11, "Escrow close request" );
        sr1Modes.put( ( byte ) 12, "Escrow open" );
        sr1Modes.put( ( byte ) 13, "Escrow close" );
        sr1Modes.put( ( byte ) 14, "Initialize start request" );
        sr1Modes.put( ( byte ) 15, "Begin initialize" );
        sr1Modes.put( ( byte ) 16, "Being set" );
        sr1Modes.put( ( byte ) 17, "Local operation" );
        sr1Modes.put( ( byte ) 18, "Storing error" );
        sr1Modes.put( ( byte ) 19, "Waiting for an envelope to set" );
    }
    static HashMap< Byte, String> sr2bits = new HashMap< Byte, String>();

    static {
        sr2bits.put( ( byte ) 0x20, "sr2 Collection End" );
        sr2bits.put( ( byte ) 0x10, "sr2 Store End" );
        sr2bits.put( ( byte ) 0x08, "sr2 Restoration End" );
        sr2bits.put( ( byte ) 0x04, "sr2 Batch End" );
        sr2bits.put( ( byte ) 0x02, "sr2 Abnormal End" );
        sr2bits.put( ( byte ) 0x01, "sr2 Count End" );
    }
    static HashMap< Byte, String> sr3bits = new HashMap< Byte, String>();

    static {
        sr3bits.put( ( byte ) 0x20, "sr3 Reject Full" );
        sr3bits.put( ( byte ) 0x10, "sr3 Escrow Full" );
        sr3bits.put( ( byte ) 0x08, "sr3 Discharging failure" );
        sr3bits.put( ( byte ) 0x04, "sr3 Rejected Bill present" );
        sr3bits.put( ( byte ) 0x02, "sr3 Escrow bill present" );
        sr3bits.put( ( byte ) 0x01, "sr3 Hopper bill present" );
    }
    static HashMap< Byte, String> sr4bits = new HashMap< Byte, String>();

    static {
        sr4bits.put( ( byte ) 0x08, "sr4 Abnormal storage" );
        sr4bits.put( ( byte ) 0x04, "sr4 Abnormal device" );
        sr4bits.put( ( byte ) 0x02, "sr4 Counting error" );
        sr4bits.put( ( byte ) 0x01, "sr4 Jamming" );
    }
    static HashMap< Byte, String> d1Modes = new HashMap< Byte, String>();

    static {
        d1Modes.put( ( byte ) 0, "Neutral/Setting mode" );
        d1Modes.put( ( byte ) 1, "Initial mode" );
        d1Modes.put( ( byte ) 2, "Deposit mode" );
        d1Modes.put( ( byte ) 3, "Manual mode" );
        d1Modes.put( ( byte ) 4, "Normal error recovery mode" );
        d1Modes.put( ( byte ) 5, "Storing error recovery mode" );
        d1Modes.put( ( byte ) 6, "Correct mode" );
    }
    static HashMap< Byte, String> d2bits = new HashMap< Byte, String>();

    static {
        d2bits.put( ( byte ) 0x20, "d2 Escrow" );
        d2bits.put( ( byte ) 0x10, "d2 Escrow shutter" );
        d2bits.put( ( byte ) 0x08, "d2 Cassette Full (sensor)" );
        d2bits.put( ( byte ) 0x04, "d2 Cassette Full (Counter)" );
    }
    CommandWithDataResponse response;
    String msg = null;

    public GloryStatus( CommandAbstract response ) {
        this.response = ( CommandWithDataResponse ) response;
    }

    public GloryStatus( CommandWithDataResponse response ) {
        this.response = response;
    }

    ArrayList< String> getBits( byte data, HashMap< Byte, String> bits ) {
        ArrayList< String> a = new ArrayList< String>();

        for ( int i = 0; i < 8; i++ ) {
            byte b = ( byte ) ( 1 << i );
            if ( ( data & b ) != 0 && bits.get( b ) != null ) {
                a.add( String.format( "%d : %s", i, bits.get( b ) ) );
            }
        }
        return a;
    }

    public void setMsg( String msg ) {
        this.msg = msg;
    }

    public boolean isError() {
        if ( response == null ) {
            return true;
        }
        return ( response.getError() != null && !response.getError().isEmpty() );
    }

    public String getError() {
        StringBuilder b = new StringBuilder();

        if ( msg != null ) {
            b.append( msg );
        }

        if ( response == null ) {
            b.append( "Invalid command response" );
        } else {
            if ( response.getError() != null ) {
                b.append( response.getError() );
            }
        }
        return b.toString();
    }

    public String getSRMode() {
        if ( response == null ) {
            return "Invalid command response";
        }
        if ( sr1Modes.get( response.getSr1() ) != null ) {
            return String.format( "Sr1 mode 0x%x %s", response.getSr1(), sr1Modes.get( response.getSr1() ) );
        } else {
            return String.format( "Sr1 mode 0x%x UNKNOWN", response.getSr1() );
        }
    }

    public String getD1Mode() {
        if ( response == null ) {
            return "Invalid command response";
        }
        if ( d1Modes.get( response.getD1() ) != null ) {
            return String.format( "d1 mode 0x%x %s", response.getD1(), d1Modes.get( response.getD1() ) );
        } else {
            return String.format( "d1 mode 0x%x UNKNOWN", response.getD1() );
        }
    }

    public ArrayList< String> getSrBits() {
        ArrayList< String> a, b = new ArrayList< String>();

        if ( response == null ) {
            return b;
        }
        a = getBits( response.getSr2(), sr2bits );
        b.addAll( a );
        a = getBits( response.getSr3(), sr3bits );
        b.addAll( a );
        a = getBits( response.getSr4(), sr4bits );
        b.addAll( a );
        return b;
    }

    public ArrayList< String> getD2Bits() {
        if ( response == null ) {
            return new ArrayList< String>();
        }
        return getBits( response.getD2(), d2bits );
    }

    public ArrayList< String> getInfo() {
        ArrayList< String> a = new ArrayList< String>();

        a.add( String.format( "d2 cassete %d", response.getD2() & 7 ) );
        a.add( String.format( "d3 currency select or country code 0x%x", response.getD3() ) );
        a.add( String.format( "d4 manual deposit number 0x%x", response.getD4() ) );
        a.add( String.format( "d5 error code outline upper 0x%x", response.getD5() ) );
        a.add( String.format( "d6 error code outline lower 0x%x", response.getD6() ) );
        a.add( String.format( "d7 error code detail upper 0x%x", response.getD7() ) );
        a.add( String.format( "d8 error code detail lower 0x%x", response.getD8() ) );
        a.add( String.format( "d9 0x%x", response.getD9() ) );
        a.add( String.format( "d10 0x%x", response.getD10() ) );
        a.add( String.format( "d11 0x%x", response.getD11() ) );
        a.add( String.format( "d12 0x%x", response.getD12() ) );
        return a;
    }

    public String getData() {
        if ( response == null ) {
            return null;
        }
        if ( !( response instanceof CommandWithDataResponse ) ) {
            return null;
        }
        byte[] data = ( ( CommandWithDataResponse ) response ).getData();
        if ( data == null || data.length == 0 ) {
            return null;
        }
        StringBuilder hexString = new StringBuilder();
        for ( byte b : data ) {
            hexString.append( " " );
            hexString.append( Integer.toHexString( 0xFF & b ) );
        }
        return hexString.toString();
    }

    public ArrayList< Integer> getBills() {
        if ( response == null ) {
            return null;
        }
        if ( !( response instanceof CommandWithCountingDataResponse ) ) {
            return null;
        }
        return ( ( CommandWithCountingDataResponse ) response ).getBills();
    }
}
