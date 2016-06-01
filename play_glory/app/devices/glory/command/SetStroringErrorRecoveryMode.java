package devices.glory.command;

/*
 *  set the mode, must be executed in 
 *  This command is setting to Deposit mode.
 *  The device will be occupied by TM, which will then wait for a
 *  processing start request from the device.
 */
public class SetStroringErrorRecoveryMode extends CommandWithAckResponse {

    public SetStroringErrorRecoveryMode() {
        super( ( byte ) 0x31, "Mode Specification (storing error recovery)" );
        byte[] b = { 0x33 };
        setCmdData( b );
    }
}
