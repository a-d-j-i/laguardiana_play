package devices.glory.command;

import java.util.Date;
import java.util.GregorianCalendar;


/*
 * Upload a file from DE to TM. If DE have file with same name, DE delete it.
 * NAK is returned when file access is failure. Refer to the Appendix 2 for the
 * file name.
 */
public class GetFileInformation extends OperationdWithDataResponse {

    public GetFileInformation(String fileName) {
        super((byte) 0x54, "GetFileInformation");
        setCmdData(fileName.getBytes());
    }
    int fileSize = -1;
    Date date = null;

    @Override
    public void setResponse(byte[] dr) {
        super.setResponse(dr);
        if (response.getError() != null) {
            return;
        }
        byte[] data = response.getData();
        if (data == null) {
            response.setError("Data is null");
            return;
        }
        if (data.length != 16) {
            response.setError(String.format("Invalid command (%s) response length %d expected 8 bytes hex number",
                    getDescription(), dr.length));
            return;
        }
        byte[] b = data;
        int l = 0;
        int i;
        for (i = 0; i < 8; i++) {
            if (b[i] >= 0x30 && b[i] <= 0x3F) {
                l += getHexDigit(b[i]) * Math.pow(16, 8 - i - 1);
            } else {
                response.setError(String.format("Invalid digit %d == 0x%x", b[i], b[i]));
            }
        }
        fileSize = l;
        int year = getDecDigit(b[i++]) * 1000 + getDecDigit(b[i++]) * 100 + getDecDigit(b[i++]) * 10 + getDecDigit(b[i++]) * 1;
        int month = getDecDigit(b[i++]) * 10 + getDecDigit(b[i++]) * 1;
        int day = getDecDigit(b[i++]) * 10 + getDecDigit(b[i++]) * 1;
        GregorianCalendar g = new GregorianCalendar(year, month, day);
        date = g.getTime();
        return;
    }

    public int getFileSize() {
        return fileSize;
    }

    public Date getDate() {
        return date;
    }
}
