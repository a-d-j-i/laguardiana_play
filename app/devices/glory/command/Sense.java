package devices.glory.command;


/*       
 * get the machine status, including the counting data if available
 * A state of the device (SR1 to SR4 and D1 to D12) is transmitted.
 * If there is no end information, the response pattern c is used.
 * If there is any end information, the response pattern d is used.
 */
public class Sense extends CommandWithDataResponse {

    public Sense() {
        super( ( byte ) 0x40, "Sense" );
    }
}
