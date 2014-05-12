package devices.glory.operation;


/*    
 * This command opens the escrow door.
 */
public class OpenEscrow extends OperationWithAckResponse {

    public OpenEscrow() {
        super(0x35);
    }
}
