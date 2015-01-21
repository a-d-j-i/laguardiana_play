package devices.glory.operation;

/*
 *  set the mode, must be executed in 
 *  This command is setting to Deposit mode.
 *  The device will be occupied by TM, which will then wait for a
 *  processing start request from the device.
 */
public class SetDepositMode extends OperationWithAckResponse {

    public SetDepositMode() {
        super(0x31);
    }

    @Override
    public byte[] getCmdStr() {
        return getCmdStrFromData(new byte[]{0x30});
    }
}