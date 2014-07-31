package devices.glory;

import devices.device.DeviceSerialPortAbstract;
import devices.device.state.DeviceStateInterface;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.state.GloryDE50StateOpenPort;
import devices.glory.task.GloryDE50TaskOperation;
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
        Logger.debug(message, args);
    }

    public GloryDE50Device() {
        super(new GloryDE50Parser(), new PortConfiguration(
                SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7,
                SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN)
        );
    }

    @Override
    public DeviceStateInterface getInitState() {
        return new GloryDE50StateOpenPort(this);
    }

    public String writeOperation(GloryDE50TaskOperation op, boolean debug) {
        if (op == null) {
            throw new InvalidParameterException("Glory unknown command");
        }
        byte[] d = op.getOperation().getCmdStr();
        if (!write(d)) {
            return String.format("Error writting to port: %s", toString());
        }
        if (debug) {
            StringBuilder h = new StringBuilder("Writed ");
            for (byte x : d) {
                h.append(String.format("0x%x ", x));
            }
            Logger.debug(h.toString());
        }
        Logger.debug("CMD : %s", op.toString());
        return null;
    }

    private boolean closing;

    public boolean isClosing() {
        return closing;
    }

    public void setClosing(boolean b) {
        closing = b;
    }

    @Override
    public String toString() {
        return "GloryDE50Device : " + super.toString();
    }

}
