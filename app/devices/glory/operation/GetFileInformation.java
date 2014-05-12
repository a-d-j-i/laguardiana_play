package devices.glory.operation;

import devices.glory.response.GloryDE50OperationResponse;
import java.util.Date;
import java.util.GregorianCalendar;


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

    @Override
    public GloryDE50OperationResponse getResponse(byte[] dr) {
        GloryDE50OperationResponse response = super.getResponse(dr);
        if (response.isError()) {
            return response;
        }

        byte[] data = response.getData();
        if (data == null) {
            return new GloryDE50OperationResponse("Data is null");
        }
        if (data.length != 16) {
            return new GloryDE50OperationResponse(String.format("Invalid command (%s) response length %d expected 8 bytes hex number", getDescription(), dr.length));
        }
        byte[] b = data;
        int l = 0;
        int i;
        for (i = 0; i < 8; i++) {
            if (b[i] >= 0x30 && b[i] <= 0x3F) {
                l += getHexDigit(b[i]) * Math.pow(16, 8 - i - 1);
            } else {
                return new GloryDE50OperationResponse(String.format("Invalid digit %d == 0x%x", b[i], b[i]));
            }
        }
        response.setFileSize(l);
        int year = getDecDigit(b[i++]) * 1000 + getDecDigit(b[i++]) * 100 + getDecDigit(b[i++]) * 10 + getDecDigit(b[i++]) * 1;
        int month = getDecDigit(b[i++]) * 10 + getDecDigit(b[i++]) * 1;
        int day = getDecDigit(b[i++]) * 10 + getDecDigit(b[i++]) * 1;
        GregorianCalendar g = new GregorianCalendar(year, month, day);
        response.setDate(g.getTime());
        return response;
    }
}
