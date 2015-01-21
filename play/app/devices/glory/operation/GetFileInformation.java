package devices.glory.operation;


/*
 * Upload a file from DE to TM. If DE have file with same name, DE delete it.
 * NAK is returned when file access is failure. Refer to the Appendix 2 for the
 * file name.
 */
public class GetFileInformation extends OperationdWithDataResponse {

    final String fileName;

    public GetFileInformation(String fileName) {
        super(0x54);
        this.fileName = fileName;
    }

    @Override
    public byte[] getCmdStr() {
        return getCmdStrFromData(fileName.getBytes());
    }
}
