package devices.glory.command;

/*
 * Packet No Offset number by 512 bytes from beginning-of-file.
 * (00000000h-FFFFFFFFh) Data 512 bytes data which is read in the file is
 * divided into upper 4 bits and lower 4 bits. h: upper 4bits, l: lower 4bits
 *
 * Download a file from TM to DE. NAK is returned when DE is not started
 * download by the command 'Start Download'. If Packet No is not sequence number
 * from the last command, NAK is returned.
 */
public class RequestDownload extends CommandWithDataResponse {

    public RequestDownload(long packetNo, byte[] data) {
        // data = data[ 1024 ]
        super((byte) 0x49, "RequestDownload");
        if (data.length != 512) {
            setError("Data must have 512 bytes");
            return;
        }
        StringBuilder hexString = new StringBuilder(String.format("%08X", packetNo));
        for (byte b : data) {
//            final String conv = "0123456789ABCDEF";
//            byte high = (byte) ((b & 0xF0) >> 4);
//            byte low = (byte) (b & 0x0F);
//            hexString.append("3");
//            hexString.append(conv.charAt(high));
//            hexString.append("3");
//            hexString.append(conv.charAt(low));
            hexString.append(Integer.toHexString(0xFF & b));
        }
        setCmdData(hexString.toString().getBytes());
    }
}
