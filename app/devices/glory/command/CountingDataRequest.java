package devices.glory.command;

/*       
 * End information and counting data for the command are transmitted.
 * If the device is in error state, NAK is responded.
 * SR2 (end information) for the counting data request command shall be
 * fixed to normal end. However, after counting stops when a batch ends,
 * SR2 (batch end) shall also accompany.
 */
public class CountingDataRequest extends OperationWithCountingDataResponse {

    public CountingDataRequest() {
        super( ( byte ) 0x41, "CountingDataRequest" );
    }
}
