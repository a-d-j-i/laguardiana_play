package devices.glory.operation;

/*    
 * This command closes the escrow door.
 * Upon SR1/ Closing the escrow door request from the device, TM transmits.
 * (For SR1, refer to 3-3-5. Details of Responses.)
 */
public class CloseEscrow extends OperationWithAckResponse {

    public CloseEscrow() {
        super(0x36);
    }
}
