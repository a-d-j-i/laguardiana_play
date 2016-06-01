package devices.glory.command;


/*    
 * This command opens the escrow door.
 */
public class OpenEscrow extends CommandWithAckResponse {

    public OpenEscrow() {
        super( ( byte ) 0x35, "Open Escrow" );
    }
}
