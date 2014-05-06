package devices.glory.command;

/*   
 * Sending machine cash amount data and end information, when
 * command received .
 * If the device is in error state, NAK is responded.
 * RETURN : 4digitx64denominations+4digitxnumber of envelopes
 */
public class AmountRequest extends OperationWithCountingDataResponse {

    public AmountRequest() {
        super( ( byte ) 0x42, "AmountRequest" );
    }
}
