package devices.glory;

import devices.device.DeviceResponseInterface;
import devices.glory.response.GloryDE50AcceptorMsg;
import devices.glory.response.GloryDE50MsgError;
import devices.glory.response.GloryDE50MsgTimeout;
import devices.serial.SerialPortAdapterInterface;
import devices.serial.SerialPortMessageParserInterface;
import java.util.concurrent.TimeoutException;
import play.Logger;

public class GloryDE50Parser implements SerialPortMessageParserInterface {

    final boolean debug = true;

    private void debug(String message, Object... args) {
        if (debug) {
            Logger.debug(message, args);
        }
    }
    final private static int GLORY_READ_TIMEOUT = 1000;

    public DeviceResponseInterface getResponse(SerialPortAdapterInterface serialPort) throws InterruptedException {
        DeviceResponseInterface ret;
        try {
            ret = getMessageInt(serialPort);
        } catch (TimeoutException ex) {
            return new GloryDE50MsgTimeout();
        }
        debug("%s Received msg : %s == %s", this.toString(), ret.getClass().getSimpleName(), ret.toString());
        return ret;
    }

    private DeviceResponseInterface getMessageInt(SerialPortAdapterInterface serialPort) throws TimeoutException, InterruptedException {
        byte[] b = null;
        for (int i = 0; i < 512; i++) {
            Byte r = read(serialPort);
            if (r == null) {
                return new GloryDE50MsgError(String.format("Error reading from port: %s", serialPort));
            } else {
                switch (r) {
                    case 0x02:
                        byte[] a = new byte[3];
                        a[ 0] = read(serialPort);
                        a[ 1] = read(serialPort);
                        a[ 2] = read(serialPort);
                        int l = getXXVal(a);
                        //Logger.debug("Read len %d", l);
                        b = new byte[l + 5];
                        b[0] = a[0];
                        b[1] = a[1];
                        b[2] = a[2];
                        for (int j = 0; j < l + 2; j++) {
                            b[ j + 3] = read(serialPort);
                        }
                        break;
                    case 0x06:
                        b = new byte[]{0x06};
                        break;
                    case 0x15:
                        b = new byte[]{0x15};
                        break;
                    default:
                        Logger.debug("Readerd 0x%x when expecting 0x02", r);
                }
            }
            if (b != null) {
                break;
            }
        }
        if (b == null) {
            return new GloryDE50MsgError("Error parsing bytes");
        }
        if (debug) {
            StringBuilder h = new StringBuilder("Readed ");
            for (byte x : b) {
                h.append(String.format("0x%x ", x));
            }
            Logger.debug(h.toString());
        }
        return new GloryDE50AcceptorMsg(b, b.length);
    }

    private Byte read(SerialPortAdapterInterface serialPort) throws TimeoutException, InterruptedException {
        if (serialPort == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        Byte ch = serialPort.read(GLORY_READ_TIMEOUT);
        if (ch == null) {
            throw new TimeoutException("timeout reading from port");
        }
//        debug("readed ch : 0x%x", ch);
        return ch;
    }

    private Integer getXXVal(byte[] b) {
        int l = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] >= 0x70 && b[i] < 0x80) {
                l += (b[i] - 0x70) * Math.pow(16, b.length - i - 1);
            } else if (b[i] >= 0x30 && b[i] < 0x3A) {
                l += (b[i] - 0x30) * Math.pow(10, b.length - i - 1);
            } else {
                return null;
            }
        }
        return l;
    }

    /*
     public byte[] getBytes(int quantity) {
     if (serialPort == null) {
     throw new InvalidParameterException("Glory Serial port closed");
     }
     byte[] b = new byte[quantity];
     for (int i = 0; i < quantity; i++) {
     b[ i] = read();
     }
     return b;
     }
     */
    @Override
    public String toString() {
        return "GloryDE50Parser";
    }

}
