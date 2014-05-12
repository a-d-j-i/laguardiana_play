/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei;

import devices.device.DeviceAbstract;
import devices.device.DeviceClassCounterIntreface;
import devices.device.DeviceStatus;
import devices.device.state.DeviceStateAbstract;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import machines.Machine;
import models.Configuration;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbds extends DeviceAbstract implements DeviceClassCounterIntreface {

    public enum MessageType {

        HostToAcceptor(0x10),
        AcceptorToHost(0x20),
        BookmarkSelected(0x30),
        CalibrateMode(0x40),
        FlashDownload(0x50),
        Request(0x60),
        Extended(0x70);
        private int id;

        private MessageType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

    };
    final private SerialPortAdapterAbstract.PortConfiguration portConf = new SerialPortAdapterAbstract.PortConfiguration(SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7, SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN);
    final private static int MEI_EBDS_READ_TIMEOUT = 35; //35ms
    private SerialPortAdapterInterface serialPort = null;

    public MeiEbds(Machine.DeviceDescription deviceDesc) {
        super(deviceDesc);
    }

    @Override
    public DeviceStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DeviceStateAbstract init() {
        return new OpenPort(new GloryDE50StateMachineApi());
    }

    @Override
    public void finish() {
        Logger.debug("Executing disassemble");
        close();
    }

    // TODO: WRONG !!!
    synchronized public boolean open(String value) {
        Logger.debug("api open");
        SerialPortAdapterInterface serialPort = Configuration.getSerialPort(value, portConf);
        Logger.info(String.format("Configuring serial port %s", serialPort));
        Logger.info("Opening glory serial port %s", serialPort);
        if (serialPort == null || this.serialPort != null) {
            return false;
        }
        this.serialPort = serialPort;
        return serialPort.open();
    }

    // TODO: WRONG !!!
    public synchronized void close() {
        Logger.info("Closing glory serial port ");
        if (serialPort != null) {
            serialPort.close();
        }
        serialPort = null;
    }

    private Byte read() {
        return serialPort.read(MEI_EBDS_READ_TIMEOUT);
    }

    public synchronized MeiEBDSDE50OperationResponse sendOperation(MeiEBDSOperationAbstract cmd) {
        return sendOperation(cmd, false);
    }

    public synchronized MeiEBDSDE50OperationResponse sendOperation(MeiEBDSOperationAbstract cmd, boolean debug) {
        if (cmd == null) {
            throw new InvalidParameterException("MeiEBDS unknown command");
        }
        if (serialPort == null) {
            return new MeiEBDSDE50OperationResponse("MeiEBDS Serial port closed");
        }
        byte[] d = cmd.getCmdStr();
        if (!serialPort.write(d)) {
            Logger.debug("Error writting to port: %s", serialPort);
            return new MeiEBDSDE50OperationResponse("Error writting from port");
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
                return new MeiEBDSDE50OperationResponse("Error reading from port");
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
            return new MeiEBDSDE50OperationResponse("MeiEBDS: response not found");
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
     throw new InvalidParameterException("MeiEBDS Serial port closed");
     }
     byte[] b = new byte[quantity];
     for (int i = 0; i < quantity; i++) {
     b[ i] = read();
     }
     return b;
     }
     */

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean envelopeDeposit() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean collect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean storingErrorReset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Integer getCurrency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void cancelCommand() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean withdrawDeposit() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized MeiEbdsMessageAbstract sendCommand(MeiEbdsMessageAbstract cmd) {
        return sendCommand(cmd, null);
    }

    public synchronized MeiEbdsMessageAbstract sendCommand(MeiEbdsMessageAbstract cmd, String data) {
        /*        if (cmd == null) {
         throw new InvalidParameterException("Mei unknown command");
         }
         if (serialPort == null) {
         cmd.setError("Mei Serial port closed");
         return cmd;
         }
         byte[] d = cmd.getCmdStr();
         try {
         serialPort.write(d);
         } catch (IOException e) {
         cmd.setError(String.format("Error writing to port %s", e.getMessage()));
         try {
         serialPort.reconect();
         } catch (IOException ex) {
         cmd.setError(String.format("Error reconecting to port %s", ex.getMessage()));
         }
         return cmd;
         }

         // debug
         if (true) {
         StringBuilder h = new StringBuilder("Writed ");
         for (byte x : d) {
         h.append(String.format("0x%x ", x));
         }
         Logger.debug(h.toString());
         Logger.debug(String.format("CMD %s", cmd.toString()));
         }

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
         cmd.setError("Mei: response not found");
         }
         // debug
         if (true) {
         StringBuilder h = new StringBuilder("Readed ");
         for (byte x : b) {
         h.append(String.format("0x%x ", x));
         }
         Logger.debug(h.toString());
         }
         return cmd.setResult(b);
         */
        return null;
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
            throw new InvalidParameterException("Mei Serial port closed");
        }
        byte[] b = new byte[quantity];
        for (int i = 0; i < quantity; i++) {
            b[ i] = read();
        }
        return b;
    }

    private byte read() throws IOException {
        return serialPort.read(READ_TIMEOUT);
    }

    protected SerialPortAdapterInterface serialPort = null;
    Queue<MeiEbdsMessageAbstract> messageQueue = new ConcurrentLinkedQueue<MeiEbdsMessageAbstract>();

    @Override
    protected boolean changeProperty(String property, String value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean cancelDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean clearError() {
        return false;
    }

}
