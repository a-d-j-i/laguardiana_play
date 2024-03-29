package devices.glory.command;

/*
 *  set the mode, must be executed in 
 *  This command is setting to Deposit mode.
 *  The device will be occupied by TM, which will then wait for a
 *  processing start request from the device.
 */
public class SetErrorRecoveryMode extends CommandWithAckResponse {

    public SetErrorRecoveryMode() {
        super( ( byte ) 0x31, "Mode Specification (error recovery)" );
        byte[] b = { 0x32 };
        setCmdData( b );
    }
}
