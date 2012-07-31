package devices.glory.command;

import devices.glory.command.GloryCommandAbstract.DebugLevel;
import java.util.ArrayList;
import play.Logger;

public class CommandWithCountingDataResponse extends CommandWithDataResponse {

    static public class Bill {

        public int idx;
        public Integer value;
    }
    ArrayList< Bill> bills = new ArrayList< Bill>();

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
                Bill b = new Bill();
                b.idx = i;
                b.value = getDigit( data[ 3 * i] ) * 100 + getDigit( data[ 3 * i + 1] ) * 10
                        + getDigit( data[ 3 * i + 2] );
                bills.add( b );
                if ( debug.isGratherThan( DebugLevel.PRINT_INFO ) ) {
                    Logger.debug( String.format( "readed bill %d %d", b.idx, b.value ) );
                }
                if ( debug.isGratherThan( DebugLevel.NONE ) ) {
                    Logger.debug( String.format( "Bill %d: quantity %d", b.idx, b.value ) );
                }
            }
        } else if ( data.length == 65 * 4 ) {
            for ( int i = 0; i < 65; i++ ) {
                Bill b = new Bill();

                b.idx = i;
                b.value = getDigit( data[ 4 * i] ) * 1000 + getDigit( data[ 4 * i + 1] ) * 100
                        + getDigit( data[ 4 * i + 2] ) * 10 + getDigit( data[ 4 * i + 3] );
                bills.add( b );
                if ( debug.isGratherThan( DebugLevel.PRINT_INFO ) ) {
                    Logger.debug( String.format( "readed bill %d %d", b.idx, b.value ) );
                }
                if ( debug.isGratherThan( DebugLevel.NONE ) ) {
                    Logger.debug( String.format( "Bill %d: quantity %d", b.idx, b.value ) );
                }
            }
        } else {
            setError( String.format( "Invalid command (%s) response length %d expected", getDescription(), dr.length ) );
        }
        return this;
    }

    public ArrayList< Bill> getBills() {
        return bills;
    }
}
