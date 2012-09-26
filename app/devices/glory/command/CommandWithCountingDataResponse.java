package devices.glory.command;

import devices.glory.command.GloryCommandAbstract.DebugLevel;
import java.util.HashMap;
import java.util.Map;
import play.Logger;

public class CommandWithCountingDataResponse extends CommandWithDataResponse {

    Map<Integer, Integer> bills = new HashMap<Integer, Integer>();

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
            for ( int slot = 0; slot < 32; slot++ ) {
                Integer value = getDecDigit( data[ 3 * slot] ) * 100
                        + getDecDigit( data[ 3 * slot + 1] ) * 10
                        + getDecDigit( data[ 3 * slot + 2] );
                bills.put( slot, value );
                if ( debug.isGratherThan( DebugLevel.PRINT_INFO ) ) {
                    Logger.debug( String.format( "readed bill %d %d", slot, value ) );
                }
                if ( debug.isGratherThan( DebugLevel.NONE ) ) {
                    Logger.debug( String.format( "Bill %d: quantity %d", slot, value ) );
                }
            }
        } else if ( data.length == 65 * 4 ) {
            for ( int slot = 0; slot < 65; slot++ ) {
                Integer value = getHexDigit( data[ 4 * slot] ) * 1000
                        + getHexDigit( data[ 4 * slot + 1] ) * 100
                        + getHexDigit( data[ 4 * slot + 2] ) * 10
                        + getHexDigit( data[ 4 * slot + 3] );
                bills.put( slot, value );
                if ( debug.isGratherThan( DebugLevel.PRINT_INFO ) ) {
                    Logger.debug( String.format( "readed bill %d %d", slot, value ) );
                }
                if ( debug.isGratherThan( DebugLevel.NONE ) ) {
                    Logger.debug( String.format( "Bill %d: quantity %d", slot, value ) );
                }
            }
        } else {
            setError( String.format( "Invalid command (%s) response length %d expected", getDescription(), dr.length ) );
        }
        return this;
    }

    public Map<Integer, Integer> getBills() {
        return bills;
    }
}
