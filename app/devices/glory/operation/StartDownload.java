package devices.glory.operation;

import devices.glory.response.GloryDE50OperationResponse;
import play.Logger;


/*
 * You set the file size by BINAY. (00000001h-FFFFFFFFh) You set the file name
 * by ASCII. Download a file from TM to DE. Set the file name by 8.3 format. If
 * DE have file with same name, DE delete it. NAK is returned when file access
 * is failure.
 */
public class StartDownload extends OperationWithAckResponse {

    final int fileSize;
    final String fileName;

    public StartDownload(int fileSize, String fileName) {
        super(0x47);
        this.fileSize = fileSize;
        this.fileName = fileName;
    }

    @Override
    public byte[] getCmdStr() {
        if (fileName.length() != 12) {
            throw new IllegalArgumentException("The file name must be in 8.3 format");
        }
        byte[] a, b;
        a = getXXFormat(fileSize, 0x30, 8);
        b = fileName.getBytes();
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        Logger.debug("File size : %d, string %s", fileSize, c.toString());
        return getCmdStrFromData(c);
    }
}
