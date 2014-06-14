package devices.glory;

import devices.device.DeviceAbstract;
import devices.device.DeviceClassCounterIntreface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.operation.OperationWithAckResponse;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.GloryDE50OpenPort;
import devices.glory.status.GloryDE50Status;
import devices.glory.task.GloryDE50TaskCount;
import devices.glory.task.GloryDE50TaskOperation;
import devices.glory.task.GloryDE50TaskStoreDeposit;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import models.Configuration;
import models.db.LgDevice;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50Device extends DeviceAbstract implements DeviceClassCounterIntreface {

    public enum GloryDE50TaskType {

        TASK_OPEN_PORT,
        TASK_COUNT,
        TASK_COLLECT,
        TASK_ENVELOPE_DEPOSIT,
        TASK_RESET,
        TASK_STORING_ERROR_RESET,
        TASK_STORE_DEPOSIT,
        TASK_WITHDRAW_DEPOSIT,
        TASK_OPERATION, TASK_CLEAR_ERROR, TASK_CANCEL;
    }

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

    public GloryDE50Device(String machineDeviceId, LgDevice.DeviceType deviceType) {
        super(machineDeviceId, deviceType);
    }

    @Override
    protected boolean changeProperty(String property, String value) throws InterruptedException, ExecutionException {
        if (property.compareToIgnoreCase("port") == 0) {
            DeviceTaskAbstract deviceTask = new DeviceTaskOpenPort(GloryDE50TaskType.TASK_OPEN_PORT, value);
            return submit(deviceTask).get();
        }
        return false;
    }

    @Override
    public DeviceStateInterface initState() {
        return new GloryDE50OpenPort(api);
    }

    public void init() {
        String initialPortValue;
        LgDeviceProperty lgSerialPort = LgDeviceProperty.getOrCreateProperty(lgd, "port", LgDeviceProperty.EditType.STRING);
        initialPortValue = lgSerialPort.value;
        Logger.debug("MeiEbds Executing init");
        submit(new DeviceTaskOpenPort(GloryDE50TaskType.TASK_OPEN_PORT, initialPortValue));
    }

    @Override
    public void finish() {
        Logger.debug("Executing GotoNeutral command on Stop");
        api.close();
        //   currentCommand = new GotoNeutral(threadCommandApi);
    }

    public Future<Boolean> count(Integer currency, Map<String, Integer> desiredQuantity) {
        DeviceTaskAbstract deviceTask = new GloryDE50TaskCount(GloryDE50TaskType.TASK_COUNT, desiredQuantity, currency);
        return submit(deviceTask);
    }

    public Future<Boolean> envelopeDeposit() {
        return submitSimpleTask(GloryDE50TaskType.TASK_ENVELOPE_DEPOSIT);
    }

    public Future<Boolean> collect() {
        return submitSimpleTask(GloryDE50TaskType.TASK_COLLECT);
    }

    public Future<Boolean> errorReset() {
        return submitSimpleTask(GloryDE50TaskType.TASK_RESET);
    }

    public boolean clearError() {
        try {
            return submitSimpleTask(GloryDE50TaskType.TASK_CLEAR_ERROR).get();
        } catch (InterruptedException ex) {
            Logger.error("Exception in clearError %s", ex);
        } catch (ExecutionException ex) {
            Logger.error("Exception in clearError %s", ex);
        }
        return false;
    }

    public Future<Boolean> storingErrorReset() {
        return submitSimpleTask(GloryDE50TaskType.TASK_STORING_ERROR_RESET);
    }

    public Future<Boolean> storeDeposit(Integer sequenceNumber) {
        DeviceTaskAbstract deviceTask = new GloryDE50TaskStoreDeposit(GloryDE50TaskType.TASK_COUNT, sequenceNumber);
        return submit(deviceTask);
    }

    public Future<Boolean> withdrawDeposit() {
        return submitSimpleTask(GloryDE50TaskType.TASK_WITHDRAW_DEPOSIT);
    }

    public Future<Boolean> cancelDeposit() {
        return submitSimpleTask(GloryDE50TaskType.TASK_CANCEL);
    }

    public GloryDE50TaskOperation sendGloryDE50Operation(OperationWithAckResponse c) {
        GloryDE50TaskOperation deviceTask = new GloryDE50TaskOperation(GloryDE50TaskType.TASK_OPERATION, c, true);
        try {
            submit(deviceTask).get();
            return deviceTask;
        } catch (InterruptedException ex) {
            Logger.error("exeption in sendGloryDE50Operation %s", ex);
        } catch (ExecutionException ex) {
            Logger.error("exeption in sendGloryDE50Operation %s", ex);
        }
        return null;
    }

}
