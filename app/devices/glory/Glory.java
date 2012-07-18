package devices.glory;

import devices.SerialPortAdapter;
import devices.glory.command.CommandAbstract;
import java.io.IOException;
import java.security.InvalidParameterException;
import play.Logger;

/*
 * TODO: getCh, fifo, etc in other class.
 */
public class Glory {
    SerialPortAdapter serialPort = null;

    public Glory( SerialPortAdapter serialPort ) {
        if ( serialPort == null ) {
            throw new InvalidParameterException( "Glory invalid parameter serial port" );
        }
        if ( this.serialPort != null ) {
            throw new InvalidParameterException( "Glory serial port allready open" );
        }
        this.serialPort = serialPort;
    }

    public synchronized CommandAbstract sendCommand( CommandAbstract cmd ) {
        return sendCommand( cmd, null, false );
    }

    public synchronized CommandAbstract sendCommand( CommandAbstract cmd, boolean debug ) {
        return sendCommand( cmd, null, debug );
    }

    public synchronized CommandAbstract sendCommand( CommandAbstract cmd, String data, boolean debug ) {

        if ( cmd == null ) {
            throw new InvalidParameterException( "Glory unknown command" );
        }

        byte[] d = cmd.getCmdStr();
        try {
            serialPort.write( d );
        } catch ( IOException e ) {
            cmd.setError( "Error writing to port" );
            return cmd;
        }

        if ( debug ) {
            StringBuilder h = new StringBuilder( "Writed " );
            for( byte x : d ) {
                h.append( String.format( "0x%x ", x ) );
            }
            Logger.debug( h.toString() );
        }
        cmd.printCmd();

        byte[] b = null;
        for( int i = 0; i < 512; i++ ) {
            try {
                byte r = serialPort.read();
                Logger.debug( String.format( "Readed 0x%x", r ) );
                switch ( r ) {
                    case 0x02:
                        byte d1 = getDigit();
                        byte d2 = getDigit();
                        byte d3 = getDigit();
                        int l = d1 * 100 + d2 * 10 + d3 * 1;
                        Logger.debug( String.format( "Read len %d", l ) );
                        b = new byte[ l + 5 ];
                        b[ 0 ] = ( byte ) ( d1 + 0x30 );
                        b[ 1 ] = ( byte ) ( d2 + 0x30 );
                        b[ 2 ] = ( byte ) ( d3 + 0x30 );
                        for( int j = 0; j < l + 2; j++ ) {
                            b[ j + 3 ] = serialPort.read();
                        }
                        break;
                    case 0x06:
                        b = new byte[] { 0x06 };
                        break;
                    case 0x15:
                        b = new byte[] { 0x15 };
                        break;
                }
                if ( b != null ) {
                    break;
                }
            } catch ( IOException e ) {
                cmd.setError( "Error reading from port" );
                return cmd;
            }
        }
        if ( b == null ) {
            cmd.setError( "Glory: response not found" );
        }
        if ( debug ) {
            StringBuilder h = new StringBuilder( "Readed " );
            for( byte x : b ) {
                h.append( String.format( "0x%x ", x ) );
            }
            Logger.debug( h.toString() );
        }
        return cmd.setResult( b );
    }

    private byte getDigit() throws IOException {
        byte l = serialPort.read();
        if ( l > 0x39 || l < 0x30 ) {
            throw new IOException( "Length digit invalid" );
        }
        return ( byte ) ( l - 0x30 );
    }
    
    public byte[] getBytes( int quantity ) throws IOException {
        byte[] b = new byte[ quantity ];
        for( int i = 0; i < quantity; i++ ) {
            b[ i ] = serialPort.read();
        }
        return b;
    }
}
