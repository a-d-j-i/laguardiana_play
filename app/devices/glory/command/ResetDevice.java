package devices.glory.command;

/*    
 * This command is start recovering error status
 * Upon SR4/Abnormal storage(0) and other error bit any one(1),
 * TM transmits.
 * (For SR4, refer to 3-3-5. Details of Responses.)
 */
public class ResetDevice extends OperationWithAckResponse {

    public ResetDevice() {
        super( ( byte ) 0x38, "Reset Device" );
    }
}
