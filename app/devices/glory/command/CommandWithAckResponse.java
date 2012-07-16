package devices.glory.command;

import play.Logger;

public class CommandWithAckResponse extends CommandAbstract {

    CommandWithAckResponse( byte cmdId, String description ) {
        this( cmdId, description, null, DebugLevel.NONE );
    }

    CommandWithAckResponse( byte cmdId, String description, byte[] cmdData ) {
        this( cmdId, description, cmdData, DebugLevel.NONE );
    }

    CommandWithAckResponse( byte cmdId, String description, byte[] cmdData, DebugLevel debug ) {
        super( cmdId, description, cmdData, debug );
    }

    public CommandWithAckResponse setResult( byte[] dr ) {

        if ( dr.length != 1 ) {
            setError( String.format( "Invalid command (%s) response length %d expected ack/noack", getDescription(),
                            dr.length ) );
            return this;
        }
        if ( dr[ 0 ] == 0x6 ) {
            if ( debug.isGratherThan( DebugLevel.NONE ) ) {
                Logger.debug( String.format( "Command %s ack", getDescription() ) );
            }
            return this;
        } else if ( dr[ 0 ] == 0x15 ) {
            setError( String.format( "Command %s not acknowledged", getDescription() ) );
            return this;
        }
        setError( String.format( "Invalid command (%s) response expected ack/noack 0x%x", getDescription(), dr[ 0 ] ) );
        return this;
    }

}
