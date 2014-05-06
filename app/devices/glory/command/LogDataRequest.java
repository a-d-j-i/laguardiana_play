package devices.glory.command;


/*   
 * Packet No Offset number by 512 bytes from
 * beginning-of-file. (00000000h-FFFFFFFFh)
 * 
 * This command is return Log Data.
 * This command is used for get the Log data in the device.
 * NAK is returned when DE is not started download by the command 'Start Upload'.
 * If Packet No is not sequence number from the last command,
 * NAK is returned.
 */
public class LogDataRequest extends OperationdWithDataResponse {

    public LogDataRequest(long packetNo) {
        super((byte) 0x45, "LogDataRequest");

        setCmdData(getXXFormat(packetNo, 0x30, 8));
    }
}
