package devices.glory;

import devices.glory.response.GloryDE50Response;
import devices.glory.command.GloryOperationAbstract;
import devices.serial.SerialPortAdapterInterface;
import java.security.InvalidParameterException;
import play.Logger;

public class GloryDE50 {

    private final int readTimeout;
    private final SerialPortAdapterInterface serialPort;

    public GloryDE50(SerialPortAdapterInterface serialPort, int readTimeout) {
        if (serialPort == null) {
            throw new InvalidParameterException("Glory invalid parameter serial port");
        }
        this.serialPort = serialPort;
        this.readTimeout = readTimeout;
    }

    public synchronized boolean open() {
        Logger.info("Opening glory serial port ");
        return serialPort.open();
    }

    public synchronized void close() {
        Logger.info("Closing glory serial port ");
        serialPort.close();
    }

    public synchronized GloryDE50Response sendCommand(GloryOperationAbstract cmd) {
        return sendCommand(cmd, null, false);
    }

    public synchronized GloryDE50Response sendCommand(GloryOperationAbstract cmd, boolean debug) {
        return sendCommand(cmd, null, debug);
    }

    public synchronized GloryDE50Response sendCommand(GloryOperationAbstract cmd, String data, boolean debug) {
        if (cmd == null) {
            throw new InvalidParameterException("Glory unknown command");
        }
        if (serialPort == null) {
            return new GloryDE50Response("Glory Serial port closed");
        }
        byte[] d = cmd.getCmdStr();
        if (!serialPort.write(d)) {
            Logger.debug("Error writting to port: %s", serialPort);
            return new GloryDE50Response("Error writting from port");
        };

        if (debug) {
            StringBuilder h = new StringBuilder("Writed ");
            for (byte x : d) {
                h.append(String.format("0x%x ", x));
            }
            Logger.debug(h.toString());
        }
        cmd.printCmd();

        byte[] b = null;
        for (int i = 0; i < 512; i++) {
            Byte r = read();
            if (r == null) {
                Logger.debug("Error reading from port: %s", serialPort);
                return new GloryDE50Response("Error reading from port");
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
            return new GloryDE50Response("Glory: response not found");
        }
        if (debug) {
            StringBuilder h = new StringBuilder("Readed ");
            for (byte x : b) {
                h.append(String.format("0x%x ", x));
            }
            Logger.debug(h.toString());
        }
        cmd.setResponse(b);
        return cmd.getResponse();
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

    private Byte read() {
        return serialPort.read(readTimeout);
    }
}
