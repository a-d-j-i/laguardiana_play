package devices.glory.operation;

/*    
 * The device is placed in remote mode/neutral state.
 * (However, it will be in neutral state when all bills excluding HP bills are extracted.)
 * Return NAK response during counting.
 * The command enables a mode change
 */
public class RemoteCancel extends OperationWithAckResponse {

    public RemoteCancel() {
        super( ( byte ) 0x37, "Remote Cancel" );
    }
}
