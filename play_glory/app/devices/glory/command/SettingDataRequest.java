package devices.glory.command;


/*   
 * You set the data by ASCII.
 * Refer to the Appendix 1 for the device setting data.
 * This command is return Device Setting Data.
 */
public class SettingDataRequest extends CommandWithDataResponse {

    public SettingDataRequest( String data ){
        super( ( byte ) 0x43, "SettingDataRequest" );
        setCmdData( data.getBytes() );
    }
}
