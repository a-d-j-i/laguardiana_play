package devices.glory.command;

/*   
 * You set the file name by ASCII.
 * The device setting data is updated by the specified file.
 * Set the file name by 8.3 format.
 * NAK is returned when setting is failure.
 * Download the file by 'Start Download', 'Request Download',
 * and 'End Download', before this command..
 * Refer to the Appendix 1 for the device setting data.
 */
public class DeviceSettingDataLoad extends OperationWithAckResponse {

    public DeviceSettingDataLoad( String fileName ) {
        super( ( byte ) 0x46, "DeviceSettingDataLoad" );
        if ( fileName.length() != 12 ) {
            response.setError( "The filename must be in 8.3 format" );
        }
        setCmdData( fileName.getBytes() );
    }
}
