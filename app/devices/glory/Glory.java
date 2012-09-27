package devices.glory;

import devices.SerialPortAdapterInterface;
import devices.SerialPortAdapterInterface;
import devices.SerialPortAdapterInterface;
import devices.glory.command.GloryCommandAbstract;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.logging.Level;
import play.Logger;

/*
 * TODO: getCh, fifo, etc in other class.
 */
public class Glory {
    
    final public static int GLORY_READ_TIMEOUT = 5000;
    SerialPortAdapterInterface serialPort = null;
    
    public Glory(SerialPortAdapterInterface serialPort) {
        if (serialPort == null) {
            throw new InvalidParameterException("Glory invalid parameter serial port");
        }
        if (this.serialPort != null) {
            throw new InvalidParameterException("Glory serial port allready open");
        }
        this.serialPort = serialPort;
    }
    
    public synchronized void close() {
        Logger.info("Closing glory serial port ");
        if (serialPort != null) {
            try {
                serialPort.close();
            } catch (IOException e) {
                Logger.error("Error closing the serial port");
            }
        }
        serialPort = null;
    }
    
    public synchronized GloryCommandAbstract sendCommand(GloryCommandAbstract cmd) {
        return sendCommand(cmd, null, false);
    }
    
    public synchronized GloryCommandAbstract sendCommand(GloryCommandAbstract cmd, boolean debug) {
        return sendCommand(cmd, null, debug);
    }
    
    public synchronized GloryCommandAbstract sendCommand(GloryCommandAbstract cmd, String data, boolean debug) {
        if (cmd == null) {
            throw new InvalidParameterException("Glory unknown command");
        }
        if (serialPort == null) {
            cmd.setError("Glory Serial port closed");
            return cmd;
        }
        byte[] d = cmd.getCmdStr();
        try {
            serialPort.write(d);
        } catch (IOException e) {
            cmd.setError("Error writing to port");
            try {
                serialPort.reconect();
            } catch (IOException ex) {
                cmd.setError("Error reconecting to port");
            }
            return cmd;
        }
        
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
            try {
                byte r = read();
                switch (r) {
                    case 0x02:
                        byte[] a = new byte[3];
                        a[ 0] = read();
                        a[ 1] = read();
                        a[ 2] = read();
                        int l = getXXVal(a);
                        Logger.debug("Read len %d", l);
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
                if (b != null) {
                    break;
                }
            } catch (IOException e) {
                Logger.debug("Error reading from port: %s", e);
                cmd.setError("Error reading from port");
                return cmd;
            }
        }
        if (b == null) {
            cmd.setError("Glory: response not found");
        }
        if (debug) {
            StringBuilder h = new StringBuilder("Readed ");
            for (byte x : b) {
                h.append(String.format("0x%x ", x));
            }
            Logger.debug(h.toString());
        }
        return cmd.setResult(b);
    }
    
    private int getXXVal(byte[] b) throws IOException {
        int l = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] >= 0x70 && b[i] < 0x80) {
                l += (b[i] - 0x70) * Math.pow(16, b.length - i - 1);
            } else if (b[i] >= 0x30 && b[i] < 0x3A) {
                l += (b[i] - 0x30) * Math.pow(10, b.length - i - 1);
            } else {
                throw new IOException(String.format("Invalid digit %d == 0x%x", b[i], b[i]));
            }
        }
        return l;
    }
    
    public byte[] getBytes(int quantity) throws IOException {
        if (serialPort == null) {
            throw new InvalidParameterException("Glory Serial port closed");
        }
        byte[] b = new byte[quantity];
        for (int i = 0; i < quantity; i++) {
            b[ i] = read();
        }
        return b;
    }
    
    private byte read() throws IOException {
        return serialPort.read(GLORY_READ_TIMEOUT);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Glory other = (Glory) obj;
        if (this.serialPort != other.serialPort && (this.serialPort == null || !this.serialPort.equals(other.serialPort))) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.serialPort != null ? this.serialPort.hashCode() : 0);
        return hash;
    }
}
