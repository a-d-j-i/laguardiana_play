package devices.ioboard;

import devices.ioboard.response.IoboardCriticalResponse;
import devices.device.DeviceResponseInterface;
import static devices.ioboard.IoboardDevice.IOBOARD_READ_TIMEOUT;
import devices.ioboard.response.IoboardErrorResponse;
import devices.ioboard.response.IoboardStateResponse;
import devices.ioboard.response.IoboardStatusResponse;
import devices.serial.SerialPortAdapterInterface;
import devices.serial.SerialPortMessageParserInterface;
import java.util.concurrent.TimeoutException;
import play.Logger;

public class IoboardDeviceMei_1_0Parser implements SerialPortMessageParserInterface {

    public DeviceResponseInterface getResponse(SerialPortAdapterInterface serialPort) throws InterruptedException, TimeoutException {
        DeviceResponseInterface ret;
        try {
            ret = getMessageInt(serialPort);
        } catch (TimeoutException ex) {
            Logger.debug("%s Timeout waiting for device, retry", this.toString());
            throw ex;
        }
        Logger.debug("%s Received msg : %s == %s", this.toString(), ret.getClass().getSimpleName(), ret.toString());
        return ret;
    }

    private DeviceResponseInterface getMessageInt(SerialPortAdapterInterface serialPort) throws TimeoutException, InterruptedException {
        while (true) {
            String l = serialPort.readLine(IOBOARD_READ_TIMEOUT);
            if (l == null) {
                throw new TimeoutException("timeout reading from port");
            }
            if (l.startsWith("STATUS :") && l.length() >= 72) {
                try {
                    Integer A = Integer.parseInt(l.substring(13, 15), 16);
                    Integer B = Integer.parseInt(l.substring(21, 23), 16);
                    Integer C = Integer.parseInt(l.substring(29, 31), 16);
                    Integer D = Integer.parseInt(l.substring(37, 39), 16);
                    Integer BAG_SENSOR = Integer.parseInt(l.substring(54, 56), 16);
                    Integer BAG_STATUS = Integer.parseInt(l.substring(70, 72), 16);
                    return new IoboardStatusResponse(((D & 0x10) == 0), ((D & 0x8) == 0), null, ((D & 0x04) == 0), A, B, C, D, BAG_SENSOR, BAG_STATUS);
                } catch (NumberFormatException e) {
                    Logger.warn("checkStatus invalid number: %s", e.getMessage());
                }
            } else if (l.startsWith("STATE :") && l.length() > 45) {
                try {
                    Integer bagSt = Integer.parseInt(l.substring(12, 14), 10);
                    Boolean bagAproved = (Integer.parseInt(l.substring(27, 28), 10) == 1);
                    Integer shutterSt = Integer.parseInt(l.substring(37, 39), 10);
                    Integer lockSt = Integer.parseInt(l.substring(45, 46), 10);
                    return new IoboardStateResponse(bagSt, shutterSt, lockSt, bagAproved);
                } catch (NumberFormatException e) {
                    Logger.warn("checkStatus invalid number: %s", e.getMessage());
                }
            } else if (l.startsWith("CRITICAL :") && l.length() > 15) {
                return new IoboardCriticalResponse(l);
            } else if (l.contains("ERROR")) {
                return new IoboardErrorResponse(l);
            } else {
                if (l.length() > 0) {
                    Logger.error("IOBOARD Ignoring line (%d): %s", l.length(), l);
                }
            }
        }
    }
}
