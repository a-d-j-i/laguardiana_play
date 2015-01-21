package devices.glory.operation;

/*   
 * Upload a file from DE to TM.
 * NAK is returned when DE is not started download by the command 'Start Upload'.
 */
public class EndUpload extends OperationWithAckResponse {

    public EndUpload() {
        super(0x53);
    }
}
