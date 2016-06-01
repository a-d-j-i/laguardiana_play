package devices.glory.operation;


/*   
 * You set the data by ASCII.
 * Refer to the Appendix 1 for the device setting data.
 * This command is return Device Setting Data.
 */
public class SettingDataRequest extends OperationdWithDataResponse {

    final String data;

    public SettingDataRequest(String data) {
        super(0x43);
        this.data = data;
    }

    @Override
    public byte[] getCmdStr() {
        return getCmdStrFromData(data.getBytes());
    }
}
