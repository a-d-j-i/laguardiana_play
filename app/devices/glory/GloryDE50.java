package devices.glory;

import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50OperationResponse;
import devices.serial.SerialPortAdapterInterface;
import java.security.InvalidParameterException;
import play.Logger;

public class GloryDE50 {

    private final int readTimeout;
    private SerialPortAdapterInterface serialPort = null;

    public GloryDE50(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public synchronized boolean open(SerialPortAdapterInterface serialPort) {
        Logger.info("Opening glory serial port %s", serialPort);
        if (serialPort == null || this.serialPort != null) {
            return false;
        }
        this.serialPort = serialPort;
        return serialPort.open();
    }

    public synchronized void close() {
        Logger.info("Closing glory serial port ");
        if (serialPort != null) {
            serialPort.close();
        }
        serialPort = null;
    }

    public synchronized String sendOperation(GloryDE50OperationInterface cmd, boolean debug, final GloryDE50OperationResponse response) throws InterruptedException {
        if (cmd == null) {
            throw new InvalidParameterException("Glory unknown command");
        }
        if (serialPort == null) {
            return String.format("Error serial port: %s closed", serialPort);
        }
        byte[] d = cmd.getCmdStr();
        if (!serialPort.write(d)) {
            return String.format("Error writting to port: %s", serialPort);
        };

        if (debug) {
            StringBuilder h = new StringBuilder("Writed ");
            for (byte x : d) {
                h.append(String.format("0x%x ", x));
            }
            Logger.debug(h.toString());
        }
        Logger.debug("CMD : %s", cmd.toString());

        byte[] b = null;
        for (int i = 0; i < 512; i++) {
            Byte r = read();
            if (r == null) {
                return String.format("Error reading from port: %s", serialPort);
            } else {
                switch (r) {
                    case 0x02:
                        byte[] a = new byte[3];
                        a[ 0] = read();
                        a[ 1] = read();
                        a[ 2] = read();
                        int l = getXXVal(a);
                        //Logger.debug("Read len %d", l);
                        b = new byte[l + 5];
                        b[0] = a[0];
                        b[1] = a[1];
                        b[2] = a[2];
                        for (int j = 0; j < l + 2; j++) {
                            b[ j + 3] = read();
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
            return "Error parsing bytes";
        }
        if (debug) {
            StringBuilder h = new StringBuilder("Readed ");
            for (byte x : b) {
                h.append(String.format("0x%x ", x));
            }
            Logger.debug(h.toString());
        }
        return cmd.fillResponse(b, response);
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

    private Byte read() throws InterruptedException {
        return serialPort.read(readTimeout);
    }
}
