package devices.glory.command;

/*   
 * This command is return Denomination Data.
 */
public class DenominationDataRequest extends CommandWithCountingDataResponse {

    public DenominationDataRequest() {
        super( ( byte ) 0x44, "DenominationDataRequest" );
    }
}
