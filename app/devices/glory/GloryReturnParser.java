package devices.glory;

import devices.glory.command.CommandWithAckResponse;
import devices.glory.command.CommandWithCountingDataResponse;
import devices.glory.command.CommandWithDataResponse;
import devices.glory.command.GloryCommandAbstract;
import java.io.Serializable;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;

public class GloryReturnParser implements Serializable {

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
    static HashMap< Byte, String> d2bits = new HashMap< Byte, String>();

    static {
        d2bits.put( ( byte ) 0x20, "d2 Escrow" );
        d2bits.put( ( byte ) 0x10, "d2 Escrow shutter" );
        d2bits.put( ( byte ) 0x08, "d2 Cassette Full (sensor)" );
        d2bits.put( ( byte ) 0x04, "d2 Cassette Full (Counter)" );
    }
    String error = null;
    String msg = null;
    String SR1Mode = "Invalid mode";
    String D1Mode = "Invalid mode";
    ArrayList<String> srBits = new ArrayList<String>();
    ArrayList<String> d2Bits = new ArrayList<String>();
    ArrayList<String> info = new ArrayList<String>();
    String data = null;
    ArrayList<Integer> bills = null;
    boolean haveData = false;

    public GloryReturnParser( GloryCommandAbstract response ) {
        if ( response instanceof CommandWithCountingDataResponse ) {
            CommandWithCountingDataResponse cmd = ( CommandWithCountingDataResponse ) response;
            bills = new ArrayList<Integer>();
            bills = cmd.getBills();
        }
        if ( response instanceof CommandWithDataResponse ) {
            CommandWithDataResponse cmd = ( CommandWithDataResponse ) response;
            SR1Mode = String.format( "Sr1 mode 0x%x %s", cmd.getSr1().getByte(), cmd.getSr1().name() );
            D1Mode = String.format( "d1 mode 0x%x %s", cmd.getD1().getByte(), cmd.getD1().name() );
            ArrayList<String> a;

            a = getBits( cmd.getSr2(), sr2bits );
            srBits.addAll( a );
            a = getBits( cmd.getSr3(), sr3bits );
            srBits.addAll( a );
            a = getBits( cmd.getSr4(), sr4bits );
            srBits.addAll( a );

            d2Bits.addAll( getBits( cmd.getD2(), d2bits ) );

            info.add( String.format( "d2 cassete %d", cmd.getD2() & 7 ) );
            info.add( String.format( "d3 currency select or country code 0x%x", cmd.getD3() ) );
            info.add( String.format( "d4 manual deposit number 0x%x", cmd.getD4() ) );
            info.add( String.format( "d5 error code outline upper 0x%x", cmd.getD5() ) );
            info.add( String.format( "d6 error code outline lower 0x%x", cmd.getD6() ) );
            info.add( String.format( "d7 error code detail upper 0x%x", cmd.getD7() ) );
            info.add( String.format( "d8 error code detail lower 0x%x", cmd.getD8() ) );
            info.add( String.format( "d9 0x%x", cmd.getD9() ) );
            info.add( String.format( "d10 0x%x", cmd.getD10() ) );
            info.add( String.format( "d11 0x%x", cmd.getD11() ) );
            info.add( String.format( "d12 0x%x", cmd.getD12() ) );

            StringBuilder hexString = new StringBuilder();
            if ( cmd.getData() != null ) {
                for ( byte b : cmd.getData() ) {
                    hexString.append( " " );
                    hexString.append( Integer.toHexString( 0xFF & b ) );
                }
                data = hexString.toString();
            }
            haveData = true;
        }
        if ( response instanceof CommandWithAckResponse ) {
        }

        if ( response.getError() != null && !response.getError().isEmpty() ) {
            error = response.getError();
        }
    }

    private ArrayList<String> getBits( byte data, HashMap<Byte, String> bits ) {
        ArrayList<String> a = new ArrayList<String>();

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
        return ( error != null );
    }

    public String getError() {
        String ret;
        StringBuilder b = new StringBuilder();
        if ( msg != null ) {
            b.append( msg );
        }
        if ( error != null ) {
            b.append( error );
        }
        ret = b.toString();
        if ( ret.isEmpty() ) {
            return null;
        }
        return ret;
    }

    public String getSRMode() {
        return SR1Mode;
    }

    public String getD1Mode() {
        return D1Mode;
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

    public String getData() {
        return data;
    }

    public ArrayList< Integer> getBills() {
        return bills;
    }

    public boolean haveData() {
        return haveData;
    }
}
