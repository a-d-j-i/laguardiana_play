package devices.glory.command;


public class CommandWithFileLongResponse extends CommandWithAckResponse {
    Long longval;

    CommandWithFileLongResponse( byte cmdId, String description ) {
        super( cmdId, description );
    }

    public CommandWithAckResponse setResult( byte[] dr ) {
        if ( dr.length == 1 ) {
            super.setResult( dr );
            return this;
        }

        if ( dr.length != 8 ) {
            setError( String.format( "Invalid command (%s) response length %d expected 8 bytes hex number",
                            getDescription(), dr.length ) );
            return this;
        }

        // TODO: Finish
        this.longval = ( long ) 0;
        setError( "Unimplemented" );

        return this;
    }

    public Long getLongVal() {
        return longval;
    }

}
