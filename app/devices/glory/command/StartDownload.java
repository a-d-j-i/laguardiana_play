package devices.glory.command;

import play.Logger;


/*
 * You set the file size by BINAY. (00000001h-FFFFFFFFh) You set the file name
 * by ASCII. Download a file from TM to DE. Set the file name by 8.3 format. If
 * DE have file with same name, DE delete it. NAK is returned when file access
 * is failure.
 */
public class StartDownload extends CommandWithAckResponse {

    public StartDownload(int fileSize, String fileName) {
        super((byte) 0x47, "StartDownload");
        if (fileName.length() != 12) {
            setError("The file name must be in 8.3 format");
        }
        String s = String.format("%08X", fileSize) + fileName;
        Logger.debug("File size : %d, string %s", fileSize, s);
        setCmdData(s.getBytes());
    }
}
