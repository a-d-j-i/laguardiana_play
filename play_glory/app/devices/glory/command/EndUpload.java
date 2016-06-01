package devices.glory.command;

/*   
 * Upload a file from DE to TM.
 * NAK is returned when DE is not started download by the command 'Start Upload'.
 */
public class EndUpload extends CommandWithAckResponse {

    public EndUpload() {
        super( ( byte ) 0x53, "EndUpload" );
    }
}
