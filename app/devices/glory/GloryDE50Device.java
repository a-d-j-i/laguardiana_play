package devices.glory;

import devices.device.DeviceAbstract;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.GloryDE50OpenPort;
import devices.glory.status.GloryDE50Status;
import devices.glory.task.GloryDE50TaskCount;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import models.Configuration;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50Device extends DeviceAbstract {

    public class GloryDE50DeviceStateApi {

        final private SerialPortAdapterAbstract.PortConfiguration portConf = new SerialPortAdapterAbstract.PortConfiguration(SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7, SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN);
        boolean closing = false;
        final private int GLORY_READ_TIMEOUT = 5000;
        final private GloryDE50 gl = new GloryDE50(GLORY_READ_TIMEOUT);

        public String sendGloryDE50Operation(GloryDE50OperationInterface operation, boolean debug, final GloryDE50OperationResponse response) {
            try {
                return gl.sendOperation(operation, debug, response);
            } catch (InterruptedException ex) {
                return "Unexpected interrupt exception in sendGloryDE50Operation";
            }
        }

        public void notifyListeners(String details) {
        }

        public void notifyListeners(GloryDE50Status.GloryDE50StatusType status) {
        }

        public boolean isClosing() {
            return closing;
        }

        public void setClosing(boolean b) {
            closing = b;
        }

        public boolean open(String value) {
            Logger.debug("api open");
            SerialPortAdapterInterface serialPort = Configuration.getSerialPort(value, portConf);
            Logger.info(String.format("Configuring serial port %s", serialPort));
            gl.close();
            Logger.debug("Glory port open try");
            boolean ret = gl.open(serialPort);
            Logger.debug("Glory port open : %s", ret ? "success" : "fails");
            return ret;
        }

        void close() {
            gl.close();
        }

    }

    final GloryDE50DeviceStateApi api = new GloryDE50DeviceStateApi();

    @Override
    public List<String> getNeededProperties() {
        return Arrays.asList(new String[]{"port"});
    }

    @Override
    public boolean setProperty(String property, String value) {
        if (property.compareToIgnoreCase("port") == 0) {
            DeviceTaskAbstract deviceTask = new DeviceTaskOpenPort(value);
            return submitSynchronous(deviceTask);
        }
        return false;
    }

    @Override
    public DeviceStateInterface initState() {
        return new GloryDE50OpenPort(api);
    }

    @Override
    public void finish() {
        Logger.debug("Executing GotoNeutral command on Stop");
        api.close();
        //   currentCommand = new GotoNeutral(threadCommandApi);
    }

    public Future<Boolean> count(Integer currency, Map<String, Integer> desiredQuantity) {
        DeviceTaskAbstract deviceTask = new GloryDE50TaskCount(currency, desiredQuantity);
        return submit(deviceTask);
    }

}
