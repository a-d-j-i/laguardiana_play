package devices.glory.operation;

/*
 *  set the mode, must be executed in 
 *  This command is setting to Deposit mode.
 *  The device will be occupied by TM, which will then wait for a
 *  processing start request from the device.
 */
public class SetManualMode extends OperationWithAckResponse {

    public SetManualMode() {
        super( ( byte ) 0x31, "Mode Specification (manual)" );
        byte[] b = { 0x31 };
        setCmdData( b );
    }
}
