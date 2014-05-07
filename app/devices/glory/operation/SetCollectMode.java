package devices.glory.operation;

/*
 *  set the mode, must be executed in 
 *  This command is setting to Deposit mode.
 *  The device will be occupied by TM, which will then wait for a
 *  processing start request from the device.
 */
public class SetCollectMode extends OperationWithAckResponse {

    public SetCollectMode() {
        super( ( byte ) 0x31, "Mode Specification (collect)" );
        byte[] b = { 0x34 };
        setCmdData( b );
    }
}
