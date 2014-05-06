package devices.glory.command;


/* 
 * The command enables TM to switch currencies.
 * The command can only be accepted in neutral/while waiting.
 * Data part: Currency table number
 * If a currency table number appended to the data part is not relevant, the
 * device does not switch currencies and transmits NAK.
 */
public class SwitchCurrency extends OperationWithAckResponse {

    public SwitchCurrency( byte c ) {
        super( ( byte ) 0x39, "Switch Currency" );
        if ( c < 8 ) {
            c = ( byte ) ( 0x30 + c );
        }
        if ( c < 0x30 && c > 0x37 ) {
            response.setError( String.format( "Invlid currency 0x%x in command %s", c, getDescription() ) );
            return;
        }
        byte[] b = { c };
        setCmdData( b );
    }
}
