package devices.glory.command;

/*   
 * Set the file name by 8.3 format, or name in the Appendix 2.
 * NAK is returned when file access is failure.
 */
public class GetFileInfo extends OperationdWithDataResponse {

    GetFileInfo( String fileName ) {
        super( ( byte ) 0x54, "GetFileInfo" );
        response.setError( "Must finish GetFileInfo" );
    }
}
