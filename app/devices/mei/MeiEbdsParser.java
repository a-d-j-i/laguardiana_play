package devices.mei;

import devices.device.DeviceResponseInterface;
import devices.mei.response.MeiEbdsAcceptorMsgAck;
import devices.mei.response.MeiEbdsAcceptorMsgEnq;
import devices.mei.response.MeiEbdsAcceptorMsgError;
import devices.serial.SerialPortAdapterInterface;
import devices.serial.SerialPortMessageParserInterface;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import play.Logger;

public class MeiEbdsParser implements SerialPortMessageParserInterface {

    private void debug(String message, Object... args) {
       // Logger.debug(message, args);
    }
    final private static int MEI_EBDS_READ_TIMEOUT = 3000; //35ms

    int retries = 0;

    public DeviceResponseInterface getResponse(SerialPortAdapterInterface serialPort) throws InterruptedException, TimeoutException {
        DeviceResponseInterface ret;
        try {
            ret = getMessageInt(serialPort);
        } catch (TimeoutException ex) {
            //pool the machine.
            if (retries++ > 100) {
                return new MeiEbdsAcceptorMsgError(String.format("%s Timeout reading from port", this.toString()));
            }
            debug("%s Timeout waiting for device, retry", this.toString());
            throw ex;
        }
        retries = 0;
        debug("%s Received msg : %s == %s", this.toString(), ret.getClass().getSimpleName(), ret.toString());
        return ret;
    }

    private DeviceResponseInterface getMessageInt(SerialPortAdapterInterface serialPort) throws TimeoutException, InterruptedException {
        byte buffer[] = new byte[40];
        while (true) {
            Byte ch = read(serialPort, MEI_EBDS_READ_TIMEOUT);
            switch (ch) {
                case 0x02: // stx
                    break;
                case 0x05: // enq
                    return new MeiEbdsAcceptorMsgEnq();
                default: // keep trying
                    Logger.error("%s mei invalid stx 0x%x  ", this.toString(), ch);
                    if (ch == 0) {
                        Thread.sleep(300);
                    }
                    continue;
            }
            buffer[ 0] = ch;
            ch = read(serialPort, 120);
            byte length = ch;
            buffer[ 1] = length;
            for (int i = 0; i < Math.min(length, buffer.length) - 2; i++) {
                ch = read(serialPort, 120);
                if (ch == null) {
                    return new MeiEbdsAcceptorMsgError(String.format("%s mei invalid len %d and timeout reading", this.toString(), length));
                }
                buffer[ i + 2] = ch;
            }
            switch (length) {
                case 0x0b: // normal message
                case 30: // extendend message
                    MeiEbdsAcceptorMsgAck ret = new MeiEbdsAcceptorMsgAck();
                    // TODO: I must check the checksum first and retry.
                    if (!ret.setData(length, buffer)) {
                        return new MeiEbdsAcceptorMsgError(String.format("%s mei invalid buffer data %s", this.toString(), Arrays.toString(buffer)));
                    }
                    return ret;
                case 8: // echo of my messages, retry.
                    return new MeiEbdsAcceptorMsgEnq();
                default:
                    String err = String.format("%s mei invalid buffer len %d data %s", this.toString(), length, Arrays.toString(buffer));
                    Logger.error(err);
                    //return new MeiEbdsAcceptorMsgError(err);
                    // with mei shit happens, retry.
                    return new MeiEbdsAcceptorMsgEnq();
            }
        }
    }

    private Byte read(SerialPortAdapterInterface serialPort, int timeout) throws TimeoutException, InterruptedException {
        if (serialPort == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        Byte ch = serialPort.read(timeout);
        if (ch == null) {
            throw new TimeoutException("timeout reading from port");
        }
//        debug("readed ch : 0x%x", ch);
        return ch;
    }
}
