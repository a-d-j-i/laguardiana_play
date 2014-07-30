package devices.glory;

import devices.device.DeviceResponseInterface;
import devices.device.DeviceSerialPortAbstract;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskMessage;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50MsgTimeout;
import devices.glory.operation.GloryDE50OperationResponse;
import devices.glory.response.GloryDE50AcceptorMsg;
import devices.glory.state.GloryDE50OpenPort;
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
        return new GloryDE50OpenPort(this);
    }
    private GloryDE50TaskOperation lastTask = null;

    public void onDeviceMessageEvent(final DeviceResponseInterface response) {
        debug("onDeviceMessageEvent : %s", response.toString());
        if (response instanceof GloryDE50MsgTimeout) {
            setLastTask("timeout", null);
        } else if (response instanceof GloryDE50AcceptorMsg) {
            GloryDE50AcceptorMsg msg = (GloryDE50AcceptorMsg) response;

            if (lastTask != null) {
                GloryDE50OperationInterface lastOp = lastTask.getOperation();
                GloryDE50OperationResponse ret = new GloryDE50OperationResponse();
                String err = lastOp.fillResponse(msg.getLength(), msg.getData(), ret);
                setLastTask(err, ret);
            } else {
                runTask(new DeviceTaskMessage(null, response));
            }
        } else {
            Logger.error("invalid response type : %s", response.toString());
        }
    }

    private void setLastTask(String error, GloryDE50OperationResponse response) {
        if (lastTask != null) {
            lastTask.setResponse(error, response);
            lastTask = null;
        }
    }

    public String sendGloryDE50Operation(GloryDE50OperationInterface op, boolean debug, GloryDE50OperationResponse response) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String writeOperation(GloryDE50TaskOperation op) {
        if (op == null) {
            throw new InvalidParameterException("Glory unknown command");
        }
        if (serialPortReader == null) {
            return String.format("Error serial port: %s closed", serialPortReader);
        }
        byte[] d = op.getOperation().getCmdStr();
        if (!serialPortReader.write(d)) {
            return String.format("Error writting to port: %s", serialPortReader);
        }
        if (op.isDebug()) {
            StringBuilder h = new StringBuilder("Writed ");
            for (byte x : d) {
                h.append(String.format("0x%x ", x));
            }
            Logger.debug(h.toString());
        }
        Logger.debug("CMD : %s", op.toString());
        return null;
    }

    public boolean sendOperation(GloryDE50TaskOperation op) {
        String err = writeOperation(op);
        if (err != null) {
            op.setError(err);
            return false;
        }
        if (lastTask != null) {
            setLastTask("Only one operation at a time", null);
            return false;
        }
        lastTask = op;
        return true;
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
