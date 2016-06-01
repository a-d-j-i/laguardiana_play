package devices.glory.operation;

/*   
 * You set the file name by ASCII.
 * The device setting data is updated by the specified file.
 * Set the file name by 8.3 format.
 * NAK is returned when setting is failure.
 * Download the file by 'Start Download', 'Request Download',
 * and 'End Download', before this command..
 * Refer to the Appendix 1 for the device setting data.
 */
public class DeviceSettingDataLoad extends OperationWithAckResponse {

    final String fileName;

    public DeviceSettingDataLoad(String fileName) {
        super(0x46);
        this.fileName = fileName;
    }

    @Override
    public byte[] getCmdStr() {

        if (fileName.length() != 12) {
            throw new IllegalArgumentException("The filename must be in 8.3 format");
        }
        return getCmdStrFromData(fileName.getBytes());
    }

}
