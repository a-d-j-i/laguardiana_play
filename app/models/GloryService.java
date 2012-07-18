package models;

import devices.SerialPortAdapter;
import devices.glory.Glory;
import devices.glory.GloryStatus;
import devices.glory.command.*;
import java.io.IOException;
import java.util.GregorianCalendar;
import play.Logger;

public class GloryService {

    static Glory device = null;
    static SerialPortAdapter serialPort = null;
    static int sequenceNumber = 0;

    static public boolean open( String gloryPort ) {
        Logger.debug( "Glory service open" );
        Logger.debug( serialPort == null ? "serialPort null" : "serialPort configured" );
        Logger.debug( device == null ? "Device null" : "Device configured" );
        if ( serialPort == null ) {
            if ( gloryPort == null ) {
                Logger.error( "Must configure the GloryPort value" );
                return false;
            }

            try {
                Logger.info( String.format( "Configuring serial port %s", gloryPort ) );
                serialPort = new SerialPortAdapter( gloryPort );
            } catch ( IOException e ) {
                Logger.error( "Error opening the serial port" );
                return false;
            }
        }

        if ( device == null ) {
            Logger.info( String.format( "Configuring glory" ) );
            device = new Glory( serialPort );
        }
        return true;
    }

    static public void close() {
        try {
            Logger.info( String.format( "Closing serial port and glory" ) );
            serialPort.close();
            serialPort = null;
            device = null;
        } catch ( IOException e ) {
            Logger.error( "Error closing the serial port" );
        }
    }

    static private GloryStatus sendCommand( CommandAbstract cmd ) {
        return sendCommand( cmd, false );
    }

    static private GloryStatus sendCommand( CommandAbstract cmd, boolean debug ) {
        if ( device == null ) {
            return new GloryStatus( null );
        }
        return new GloryStatus( device.sendCommand( cmd, debug ) );
    }

    static public GloryStatus sense() {
        return sendCommand( new Sense() );
    }

    static public GloryStatus remoteCancel() {
        CommandWithAckResponse c = new devices.glory.command.RemoteCancel();
        return sendCommand( c, true );
    }

    static public GloryStatus setDepositMode() {
        CommandWithAckResponse c = new devices.glory.command.SetDepositMode();
        return sendCommand( c, true );
    }

    static public GloryStatus setManualMode() {
        CommandWithAckResponse c = new devices.glory.command.SetManualMode();
        return sendCommand( c, true );
    }

    static public GloryStatus setErrorRecoveryMode() {
        CommandWithAckResponse c = new devices.glory.command.SetErrorRecoveryMode();
        return sendCommand( c, true );
    }

    static public GloryStatus setStroringErrorRecoveryMode() {
        CommandWithAckResponse c = new devices.glory.command.SetStroringErrorRecoveryMode();
        return sendCommand( c, true );
    }

    static public GloryStatus openEscrow() {
        CommandWithAckResponse c = new devices.glory.command.OpenEscrow();
        return sendCommand( c, true );
    }

    static public GloryStatus closeEscrow() {
        CommandWithAckResponse c = new devices.glory.command.CloseEscrow();
        return sendCommand( c, true );
    }

    static public GloryStatus StroingStart() {
        CommandWithAckResponse c = new devices.glory.command.StoringStart( sequenceNumber );
        sequenceNumber++;
        return sendCommand( c, true );
    }

    static public GloryStatus StopCounting() {
        CommandWithAckResponse c = new devices.glory.command.StopCounting();
        return sendCommand( c, true );
    }

    static public GloryStatus ResetDevice() {
        CommandWithAckResponse c = new devices.glory.command.ResetDevice();
        return sendCommand( c, true );
    }

    static public GloryStatus BatchDataTransmition( int[] bills ) {
        CommandWithAckResponse c = new devices.glory.command.BatchDataTransmition( bills );
        return sendCommand( c, true );
    }

    static public GloryStatus SwitchCurrency( byte cu ) {
        CommandWithAckResponse c = new devices.glory.command.SwitchCurrency( cu );
        return sendCommand( c, true );
    }

    static public GloryStatus CountingDataRequest() {
        return sendCommand( new devices.glory.command.CountingDataRequest() );
    }

    static public GloryStatus AmountRequest() {
        return sendCommand( new devices.glory.command.AmountRequest() );
    }

    static public GloryStatus SettingDataRequest() {
        return sendCommand( new devices.glory.command.SettingDataRequest( "ESCROW_SET" ), true );
        // return sendCommand( new devices.glory.command.SettingDataRequest( "CASSETE_SET" ), true );
        // return sendCommand( new devices.glory.command.SettingDataRequest( "REJECT_SET" ), true );
    }

    static public GloryStatus DenominationDataRequest() {
        return sendCommand( new devices.glory.command.DenominationDataRequest(), true );
    }

    static public GloryStatus LogDataRequest() {
        return sendCommand( new devices.glory.command.LogDataRequest( 0 ), true );
    }

    static public GloryStatus UploadData( int fileSize, String fileName, byte[] data ) {

        CommandWithAckResponse c = new devices.glory.command.StartDownload( fileSize, fileName );
        GloryStatus st = sendCommand( c, true );
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
            st = sendCommand( c, true );
            if ( st.isError() ) {
                st.setMsg( "Error in RequestDownload" );
                return st;
            }
        }
        c = new devices.glory.command.EndDownload();
        st = sendCommand( c, true );
        if ( st.isError() ) {
            st.setMsg( "Error in EndDownload" );
            return st;
        }
        return null;
    }

    static public GloryStatus DeviceSettingDataLoad() {

        byte[] b = new byte[ 512 ];
        for ( int i = 0; i < 10; i++ ) {
            b[ i] = ( byte ) i;
        }
        UploadData( 10, "settings.txt", b );

        CommandWithAckResponse c = new devices.glory.command.DeviceSettingDataLoad( "settings.txt" );
        return sendCommand( c, true );
    }

    static public GloryStatus ProgramUpdate() {

        byte[] b = new byte[ 512 ];
        for ( int i = 0; i < 10; i++ ) {
            b[ i] = ( byte ) i;
        }
        UploadData( 10, "programs.txt", b );

        CommandWithAckResponse c = new devices.glory.command.ProgramUpdate( "programs.txt" );
        return sendCommand( c, true );
    }

    static public GloryStatus SetTime() {
        CommandWithAckResponse c = new devices.glory.command.SetTime( GregorianCalendar.getInstance().getTime() );
        return sendCommand( c, true );
    }

    static public GloryStatus DownloadData( String fileName ) {

        CommandWithFileLongResponse c = new devices.glory.command.StartUpload( fileName );
        GloryStatus st = sendCommand( c, true );
        if ( st.isError() ) {
            st.setMsg( "Error in StartDownload" );
            return st;
        }

        // TODO: ???
        long quantity = c.getLongVal();
        byte[] data;
        try {
            data = device.getBytes( ( int ) quantity );
        } catch ( IOException e ) {
            st.setMsg( "Error reading data from device" );
            return st;
        }

        EndUpload c1 = new devices.glory.command.EndUpload();
        st = sendCommand( c1, true );
        if ( st.isError() ) {
            st.setMsg( "Error in EndDownload" );
            return st;
        }
        st.setMsg( String.format( "Readed %d bytes", data.length ) );
        return st;
    }
}
