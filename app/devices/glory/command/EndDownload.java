package devices.glory.command;

/*   
 * Download a file from TM to DE.
 * NAK is returned when DE is not started download by the command 'Start Download'.
 */
public class EndDownload extends OperationWithAckResponse {

    public EndDownload() {
        super( ( byte ) 0x48, "EndDownload" );
    }
}
