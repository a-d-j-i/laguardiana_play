package devices.glory;

import devices.device.DeviceAbstract.DeviceApi;
import devices.device.DeviceMessageInterface;
import devices.device.DeviceResponseInterface;
import devices.device.status.DeviceStatusInterface;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.operation.Sense;
import devices.glory.response.GloryDE50MsgError;
import devices.glory.response.GloryDE50OperationResponse;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import devices.serial.SerialPortMessageParserInterface;
import devices.device.DeviceSerialPortAdaptor;
import java.security.InvalidParameterException;
import java.util.concurrent.TimeoutException;
import models.Configuration;
import play.Logger;

public class GloryDE50 implements SerialPortMessageParserInterface {

    final boolean debug = true;

    private void debug(String message, Object... args) {
        if (debug) {
            Logger.debug(message, args);
        }
    }
    final private SerialPortAdapterAbstract.PortConfiguration portConf = new SerialPortAdapterAbstract.PortConfiguration(
            SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7,
            SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN);
    final private static int GLORY_READ_TIMEOUT = 3000; //35ms

    private DeviceSerialPortAdaptor serialPortReader = null;
    final DeviceApi api;
    int retries = 0;

    public GloryDE50(DeviceApi api) {
        this.api = api;
    }

    public void notifyListeners(DeviceStatusInterface status) {
        api.notifyListeners(status);
    }

    private String port;
    private GloryDE50OperationInterface lastCmd;

    public synchronized boolean open(String port) {
        debug("%s api open", this.toString());
        this.port = port;
        close();
        SerialPortAdapterInterface serialPort = Configuration.getSerialPort(port, portConf);
        if (serialPort == null || this.serialPortReader != null) {
            Logger.info("%s Error opening mei serial port %s %s", this.toString(), serialPort, this.serialPortReader);
            return false;
        }
        this.serialPortReader = new DeviceSerialPortAdaptor(serialPort, this, api);
        return serialPortReader.open();
    }

    public synchronized void close() {
        if (serialPortReader != null) {
            Logger.info("%s Closing mei serial port ", this.toString());
            serialPortReader.close();
            serialPortReader = null;
        }
    }

    public DeviceResponseInterface getResponse(SerialPortAdapterInterface serialPort) throws InterruptedException {
        DeviceResponseInterface ret;
        try {
            ret = getMessageInt();
        } catch (TimeoutException ex) {
            debug("%s Timeout waiting for device, retry", this.toString());
            //pool the machine.
            if (retries++ > 100) {
                return new GloryDE50MsgError(String.format("%s Timeout reading from port", this.toString()));
            }
            String err = sendOperation(new Sense(), true);
            if (err != null) {
                return new GloryDE50MsgError(err);
            }
            return null;
        }
        retries = 0;
        debug("%s Received msg : %s == %s", this.toString(), ret.getType().name(), ret.toString());
        return ret;
    }

    private DeviceResponseInterface getMessageInt() throws TimeoutException, InterruptedException {
        byte[] b = null;
        for (int i = 0; i < 512; i++) {
            Byte r = read();
            if (r == null) {
                return new GloryDE50MsgError(String.format("Error reading from port: %s", serialPortReader));
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
            return new GloryDE50MsgError("Error parsing bytes");
        }
        if (debug) {
            StringBuilder h = new StringBuilder("Readed ");
            for (byte x : b) {
                h.append(String.format("0x%x ", x));
            }
            Logger.debug(h.toString());
        }
        GloryDE50OperationResponse ret = new GloryDE50OperationResponse();
        String err = lastCmd.fillResponse(b.length, b, ret);
        if (err != null) {
            return new GloryDE50MsgError(err);

        }
        return ret;
    }

    private Byte read() throws TimeoutException, InterruptedException {
        if (serialPortReader == null) {
            throw new IllegalArgumentException("Serial port closed");
        }
        Byte ch = serialPortReader.read(GLORY_READ_TIMEOUT);
        if (ch == null) {
            throw new TimeoutException("timeout reading from port");
        }
//        debug("readed ch : 0x%x", ch);
        return ch;
    }

    public synchronized String sendOperation(GloryDE50OperationInterface cmd, boolean debug) throws InterruptedException {
        if (cmd == null) {
            throw new InvalidParameterException("Glory unknown command");
        }
        if (serialPortReader == null) {
            return String.format("Error serial port: %s closed", serialPortReader);
        }
        byte[] d = cmd.getCmdStr();
        if (!serialPortReader.write(d)) {
            return String.format("Error writting to port: %s", serialPortReader);
        }
        lastCmd = cmd;
        if (debug) {
            StringBuilder h = new StringBuilder("Writed ");
            for (byte x : d) {
                h.append(String.format("0x%x ", x));
            }
            Logger.debug(h.toString());
        }
        Logger.debug("CMD : %s", cmd.toString());
        return null;
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
        return "GloryDE50{" + "port=" + port + '}';
    }

    DeviceMessageInterface getLastCommand() {
        return lastCmd;
    }

}
