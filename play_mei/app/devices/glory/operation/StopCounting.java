package devices.glory.operation;

/*    
 * stop the counting process
 * This command stops the counting.
 * Upon SR1/counting start request (Q) from the device, TM transmits.
 * (For SR1, refer to 3-3-5. Details of Responses.)
 * This command is used for stop the counting.
 */
public class StopCounting extends OperationWithAckResponse {

    public StopCounting() {
        super(0x33);
    }

}
