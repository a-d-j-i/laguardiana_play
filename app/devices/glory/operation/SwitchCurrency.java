package devices.glory.operation;


/* 
 * The command enables TM to switch currencies.
 * The command can only be accepted in neutral/while waiting.
 * Data part: Currency table number
 * If a currency table number appended to the data part is not relevant, the
 * device does not switch currencies and transmits NAK.
 */
public class SwitchCurrency extends OperationWithAckResponse {

    final byte currency;

    public SwitchCurrency(byte currency) {
        super(0x39);
        this.currency = currency;
    }

    @Override
    public byte[] getCmdStr() {
        byte c = currency;
        if (c < 8) {
            c = (byte) (0x30 + c);
        }
        if (c < 0x30 && c > 0x37) {
            throw new IllegalArgumentException(String.format("Invlid currency 0x%x in command %s", c, getDescription()));
        }
        return getCmdStrFromData(new byte[]{c});
    }
}
