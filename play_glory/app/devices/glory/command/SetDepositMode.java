package devices.glory.command;

/*
 *  set the mode, must be executed in 
 *  This command is setting to Deposit mode.
 *  The device will be occupied by TM, which will then wait for a
 *  processing start request from the device.
 */
public class SetDepositMode extends CommandWithAckResponse {

    public SetDepositMode() {
        super( ( byte ) 0x31, "Mode Specification (deposit)" );
        byte[] b = { 0x30 };
        setCmdData( b );
    }
}
