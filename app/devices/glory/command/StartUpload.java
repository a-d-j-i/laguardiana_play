package devices.glory.command;


/* 
 * Upload a file from DE to TM.
 * If DE have file with same name, DE delete it.
 * NAK is returned when file access is failure.
 * Refer to the Appendix 2 for the file name.
 */
public class StartUpload extends CommandWithFileLongResponse {

    public StartUpload( String fileName ) {
        super( ( byte ) 0x52, "StartUpload" );
        if ( fileName.length() != 12 ) {
            setError( "The file name must be in 8.3 format" );
        }
        setCmdData( fileName.getBytes() );
    }
}
