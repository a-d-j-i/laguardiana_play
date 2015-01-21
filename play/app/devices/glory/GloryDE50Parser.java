package devices.glory;

import devices.device.DeviceResponseInterface;
import devices.glory.response.GloryDE50Response;
import devices.glory.response.GloryDE50ResponseError;
import devices.glory.response.GloryDE50ResponseNak;
import devices.glory.response.GloryDE50ResponseWithData;
import devices.serial.SerialPortAdapterInterface;
import devices.serial.SerialPortMessageParserInterface;
import java.util.concurrent.TimeoutException;
import play.Logger;

public class GloryDE50Parser implements SerialPortMessageParserInterface {

    final boolean debug = false;

    private void debug(String message, Object... args) {
        if (debug) {
            Logger.debug(message, args);
        }
    }
    final private static int GLORY_READ_TIMEOUT = 1200;

    public DeviceResponseInterface getResponse(SerialPortAdapterInterface serialPort) throws InterruptedException, TimeoutException {
        DeviceResponseInterface ret = getMessageInt(serialPort);
        debug("%s Received msg : %s == %s", this.toString(), ret.getClass().getSimpleName(), ret.toString());
        return ret;
    }

    private DeviceResponseInterface getMessageInt(SerialPortAdapterInterface serialPort) throws TimeoutException, InterruptedException {
        byte[] b = null;
        for (int i = 0; i < 512; i++) {
            Byte r = read(serialPort);
            if (r == null) {
                return new GloryDE50ResponseError(String.format("Error reading from port: %s", serialPort));
            }
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
                    return new GloryDE50Response();
                case 0x15:
                    return new GloryDE50ResponseNak();
                default:
                    Logger.debug("Readerd 0x%x when expecting 0x02", r);
            }

            if (b != null) {
                break;
            }
        }
        if (b == null) {
            return new GloryDE50ResponseError("Error parsing bytes");
        }
        if (debug) {
            StringBuilder h = new StringBuilder("Readed ");
            for (byte x : b) {
                h.append(String.format("0x%x ", x));
            }
            Logger.debug(h.toString());
        }

        if (b[ b.length - 2] != 3) {
            return new GloryDE50ResponseError(String.format("Error message end not found"));
        }

        byte checksum = 0;
        for (int i = 0; i < b.length - 1; i++) {
            checksum = (byte) (checksum ^ b[ i]);
        }

        if (b[ b.length - 1] != (byte) checksum) {
            return new GloryDE50ResponseError(String.format("CHECKSUM don't match 0x%x != 0x%x", b[ b.length - 1], checksum));
        }
        return new GloryDE50ResponseWithData(b);
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
