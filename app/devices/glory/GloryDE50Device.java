package devices.glory;

import devices.device.DeviceResponseInterface;
import devices.device.DeviceSerialPortAbstract;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskMessage;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.GloryDE50OpenPort;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterAbstract.PortConfiguration;
import java.security.InvalidParameterException;
import play.Logger;

/**
 *
 * @author adji
 */
final public class GloryDE50Device extends DeviceSerialPortAbstract {

    private void debug(String message, Object... args) {
        // Logger.debug(message, args);
    }

    public GloryDE50Device() {
        super(new GloryDE50Parser(), new PortConfiguration(
                SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7,
                SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN)
        );
    }

    @Override
    public DeviceStateInterface getInitState() {
        return new GloryDE50OpenPort(this);
    }
    private GloryDE50OperationResponse lastResult = new GloryDE50OperationResponse();
    private GloryDE50OperationInterface lastCmd = null;

    public void onDeviceMessageEvent(final DeviceResponseInterface response) {
        lastResult = (GloryDE50OperationResponse) response;
        runTask(new DeviceTaskMessage(lastCmd, response));
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

    public String sendGloryDE50Operation(GloryDE50OperationInterface operation, boolean debug, GloryDE50OperationResponse response) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isClosing() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setClosing(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return "GloryDE50Device : " + super.toString();
    }
}
