package devices.glory.operation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/*
 * Packet No Offset number by 512 bytes from beginning-of-file.
 * (00000000h-FFFFFFFFh) Data 512 bytes data which is read in the file is
 * divided into upper 4 bits and lower 4 bits. h: upper 4bits, l: lower 4bits
 *
 * Download a file from TM to DE. NAK is returned when DE is not started
 * download by the command 'Start Download'. If Packet No is not sequence number
 * from the last command, NAK is returned.
 */
public class RequestDownload extends OperationdWithDataResponse {

    public RequestDownload(long packetNo, byte[] data) {
        super((byte) 0x49, "RequestDownload");
        if (data.length != 512) {
            response.setError("Data must have 512 bytes");
            return;
        }
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream(1050);

            os.write(getXXFormat(packetNo, 0x30, 8));
            for (byte b : data) {
                os.write((byte) ((b & 0xF0) >> 4) + 0x30);
                os.write((byte) (b & 0x0F) + 0x30);
            }
            setCmdData(os.toByteArray());
        } catch (IOException e) {
            response.setError("Generating buffer" + e.getMessage());
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
