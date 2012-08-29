package controllers;

import com.google.gson.Gson;
import devices.CounterFactory;
import devices.glory.GloryReturnParser;
import devices.glory.command.*;
import java.io.IOException;
import java.util.GregorianCalendar;
import play.Play;
import play.cache.Cache;
import play.mvc.Before;
import play.mvc.Router;

public class GloryController extends BaseController {

    static devices.glory.Glory glory;

    @Before
    static void getCounter() throws Throwable {
        if ( flash.get( "error" ) == null ) {
            glory = CounterFactory.getCounter( Play.configuration.getProperty( "glory.port" ) );
            if ( glory == null ) {
                flash.put( "error", "Error opening port" );
                redirect( Router.reverse( "GloryController.index" ).url );
            }
        } else {
            redirect( Router.reverse( "GloryController.index" ).url );
        }
    }

    public static void index() {
        GloryReturnParser status = null;
        String lastStatus = flash.get( "lastStatus" );
        String lst = null;
        if ( "true".equals( lastStatus ) ) {
            lst = ( String ) Cache.get( "lastStatus" );
            Cache.delete( "lastStatus" );
        }
        if ( lst != null ) {
            if ( !lst.equalsIgnoreCase( "null" ) ) {
                Gson gson = new Gson();
                status = gson.fromJson( lst, GloryReturnParser.class );
            }
        }
        render( status );
    }

    private static void setStatusAndRedirect( GloryReturnParser st ) {
        if ( st == null ) {
            flash.put( "lastStatus", "false" );
        } else {
            if ( st.getError() != null ) {
                flash.put( "error", st.getError() );
            } else {
                flash.put( "success", "success" );
            }
            Gson gson = new Gson();
            String json = gson.toJson( st );
            Cache.set( "lastStatus", json );
            flash.put( "lastStatus", "true" );
        }
        redirect( Router.reverse( "GloryController.index" ).url );
    }

    private static void setStatusAndRedirect( GloryCommandAbstract cmd ) {
        if ( cmd == null ) {
            flash.put( "lastStatus", "false" );
            redirect( Router.reverse( "GloryController.index" ).url );
        } else {
            GloryReturnParser st = new GloryReturnParser( cmd );
            setStatusAndRedirect( st );
        }
    }

    public static void sense() throws IOException {
        setStatusAndRedirect( glory.sendCommand( new Sense() ) );
    }

    public static void remoteCancel() {
        CommandWithAckResponse c = new devices.glory.command.RemoteCancel();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void setDepositMode() {
        CommandWithAckResponse c = new devices.glory.command.SetDepositMode();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void setManualMode() {
        CommandWithAckResponse c = new devices.glory.command.SetManualMode();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void setErrorRecoveryMode() {
        CommandWithAckResponse c = new devices.glory.command.SetErrorRecoveryMode();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void setStroringErrorRecoveryMode() {
        CommandWithAckResponse c = new devices.glory.command.SetStroringErrorRecoveryMode();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void openEscrow() {
        CommandWithAckResponse c = new devices.glory.command.OpenEscrow();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void closeEscrow() {
        CommandWithAckResponse c = new devices.glory.command.CloseEscrow();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void storingStart() {
        CommandWithAckResponse c = new devices.glory.command.StoringStart( 0 );
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void stopCounting() {
        CommandWithAckResponse c = new devices.glory.command.StopCounting();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void resetDevice() {
        CommandWithAckResponse c = new devices.glory.command.ResetDevice();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void switchCurrency( Long cu ) {
        CommandWithAckResponse c = new devices.glory.command.SwitchCurrency( cu.byteValue() );
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void batchDataTransmition() {
        int[] bills = new int[ 32 ];
        for ( int i = 0; i < bills.length; i++ ) {
            bills[ i] = 0;
        }
        bills[ 27] = 0;
        bills[ 26] = 0;

        CommandWithAckResponse c = new devices.glory.command.BatchDataTransmition( bills );
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void countingDataRequest() throws IOException {
        CommandWithAckResponse c = new devices.glory.command.CountingDataRequest();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void amountRequest() {
        CommandWithAckResponse c = new devices.glory.command.AmountRequest();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void denominationDataRequest() throws IOException {
        CommandWithAckResponse c = new devices.glory.command.DenominationDataRequest();
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void settingDataRequest() throws IOException {
        CommandWithAckResponse c = new devices.glory.command.SettingDataRequest( "ESCROW_SET" );
        //CommandWithAckResponse c = new devices.glory.command.SettingDataRequest( "CASSETE_SET" );
        //CommandWithAckResponse c = new devices.glory.command.SettingDataRequest( "REJECT_SET" );
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void logDataRequest() throws IOException {
        CommandWithAckResponse c = new devices.glory.command.LogDataRequest( 0 );
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void deviceSettingDataLoad() {
        byte[] b = new byte[ 512 ];
        for ( int i = 0; i < 10; i++ ) {
            b[ i] = ( byte ) i;
        }
        GloryReturnParser st = UploadData( 10, "settings.txt", b );
        if ( st != null ) {
            setStatusAndRedirect( st );
            return;
        }
        CommandWithAckResponse c = new devices.glory.command.DeviceSettingDataLoad( "settings.txt" );
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void programUpdate() {
        byte[] b = new byte[ 512 ];
        for ( int i = 0; i < 10; i++ ) {
            b[ i] = ( byte ) i;
        }
        GloryReturnParser st = UploadData( 10, "programs.txt", b );
        if ( st != null ) {
            setStatusAndRedirect( st );
            return;
        }
        CommandWithAckResponse c = new devices.glory.command.ProgramUpdate( "programs.txt" );
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void setTime() {
        CommandWithAckResponse c = new devices.glory.command.SetTime( GregorianCalendar.getInstance().getTime() );
        setStatusAndRedirect( glory.sendCommand( c, true ) );
    }

    public static void downloadData() {
        GloryReturnParser st = DownloadData( "settings.txt" );
        setStatusAndRedirect( st );
    }

    static private GloryReturnParser UploadData( int fileSize, String fileName, byte[] data ) {
        CommandWithAckResponse c = new devices.glory.command.StartDownload( fileSize, fileName );
        GloryReturnParser st = new GloryReturnParser( glory.sendCommand( c, true ) );
        if ( st.isError() ) {
            st.setMsg( "Error in StartDownload" );
            return st;
        }

        byte[] b = new byte[ 512 ];
        for ( int j = 0; j < ( ( fileSize + 512 ) / 512 ); j++ ) {
            // TODO: Optimize.
            for ( int i = 0; i < 512; i++ ) {
                if ( j * 512 + i < data.length ) {
                    b[ i] = data[ j * 512 + i];
                } else {
                    b[ i] = 0;
                }
            }
            c = new devices.glory.command.RequestDownload( j, b );
            st = new GloryReturnParser( glory.sendCommand( c, true ) );
            if ( st.isError() ) {
                st.setMsg( "Error in RequestDownload" );
                return st;
            }
        }
        c = new devices.glory.command.EndDownload();
        st = new GloryReturnParser( glory.sendCommand( c, true ) );
        if ( st.isError() ) {
            st.setMsg( "Error in EndDownload" );
            return st;
        }
        return null;
    }

    static private GloryReturnParser DownloadData( String fileName ) {

        CommandWithFileLongResponse c = new devices.glory.command.StartUpload( fileName );
        GloryReturnParser st = new GloryReturnParser( glory.sendCommand( c, true ) );
        if ( st.isError() ) {
            st.setMsg( "Error in StartDownload" );
            return st;
        }

        // TODO: ???
        long quantity = c.getLongVal();
        byte[] data;
        try {
            data = glory.getBytes( ( int ) quantity );
        } catch ( IOException e ) {
            st.setMsg( "Error reading data from device" );
            return st;
        }

        EndUpload c1 = new devices.glory.command.EndUpload();
        st = new GloryReturnParser( glory.sendCommand( c, true ) );
        if ( st.isError() ) {
            st.setMsg( "Error in EndDownload" );
            return st;
        }
        st.setMsg( String.format( "Readed %d bytes", data.length ) );
        return st;
    }
}
