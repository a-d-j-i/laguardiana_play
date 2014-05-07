/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei;

import devices.DeviceAbstract;
import devices.DeviceClassCounterIntreface;
import devices.DeviceEventListener;
import devices.DeviceStatus;
import devices.serial.SerialPortAdapterInterface;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbds extends DeviceAbstract implements DeviceClassCounterIntreface {

    final private int READ_TIMEOUT = 1000;

    public MeiEbds(DeviceType deviceType, String machineDeviceId) {
        super(deviceType, machineDeviceId);
    }

    @Override
    protected void initDeviceProperties() {
        LgDeviceProperty.getOrCreateProperty(lgd, "port", LgDeviceProperty.EditType.STRING);
    }

    @Override
    public DeviceStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void assemble() {
//        currentState.set(new OpenPort(new GloryDE50StateMachineApi()));
    }

    @Override
    public void mainLoop() {
        /*        Logger.debug(String.format("Glory executing current step: %s", currentState.getClass().getSimpleName()));
         GloryDE50StateAbstract oldState = currentState.get();
         GloryDE50StateAbstract newState = oldState.step();
         if (newState != null && oldState != newState) {
         GloryDE50StateAbstract initState = newState.init();
         if (initState != null) {
         newState = initState;
         }
         currentState.set(newState);
         }*/
    }

    @Override
    public void disassemble() {
        Logger.debug("Executing GotoNeutral command on Stop");
        //   currentCommand = new GotoNeutral(threadCommandApi);
    }

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
