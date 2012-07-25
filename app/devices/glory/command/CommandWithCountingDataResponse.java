package devices.glory.command;

import devices.glory.command.GloryCommandAbstract.DebugLevel;
import java.util.ArrayList;
import play.Logger;

public class CommandWithCountingDataResponse extends CommandWithDataResponse {

    ArrayList< Integer> bills = new ArrayList< Integer>();

    CommandWithCountingDataResponse( byte cmdId, String description ) {
        this( cmdId, description, null, DebugLevel.NONE );
    }

    CommandWithCountingDataResponse( byte cmdId, String description, byte[] cmdData ) {
        this( cmdId, description, cmdData, DebugLevel.NONE );
    }

    CommandWithCountingDataResponse( byte cmdId, String description, byte[] cmdData, DebugLevel debug ) {
        super( cmdId, description, cmdData, debug );
    }

    // TODO: Support amount request format
    public CommandWithCountingDataResponse setResult( byte[] dr ) {
        super.setResult( dr );
        if ( getError() != null ) {
            return this;
        }

        if ( data == null || data.length == 0 ) {
            return this;
        }

        if ( data.length == 32 * 3 ) {
            for ( int i = 0; i < 32; i++ ) {
                int v = getDigit( data[ 3 * i] ) * 100 + getDigit( data[ 3 * i + 1] ) * 10
                        + getDigit( data[ 3 * i + 2] );
                Logger.debug( String.format( "readed bill %d %d", i, v ) );
                bills.add( v );
                if ( debug.isGratherThan( DebugLevel.NONE ) ) {
                    Logger.debug( String.format( "Bill %d: quantity %d", i, v ) );
                }
            }
        } else if ( data.length == 65 * 4 ) {
            for ( int i = 0; i < 65; i++ ) {
                int v = getDigit( data[ 4 * i] ) * 1000 + getDigit( data[ 4 * i + 1] ) * 100
                        + getDigit( data[ 4 * i + 2] ) * 10 + getDigit( data[ 4 * i + 3] );
                Logger.debug( String.format( "readed bill %d %d", i, v ) );
                bills.add( v );
                if ( debug.isGratherThan( DebugLevel.NONE ) ) {
                    Logger.debug( String.format( "Bill %d: quantity %d", i, v ) );
                }
            }
        } else {
            setError( String.format( "Invalid command (%s) response length %d expected ack/noack", getDescription(),
                    dr.length ) );
        }
        return this;
    }

    public ArrayList< Integer> getBills() {
        return bills;
    }
}
