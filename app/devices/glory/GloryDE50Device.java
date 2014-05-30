package devices.glory;

import devices.device.DeviceAbstract;
import devices.device.DeviceClassCounterIntreface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.glory.task.GloryDE50TaskCount;
import devices.glory.operation.OperationWithAckResponse;
import devices.glory.state.GloryDE50OpenPort;
import devices.device.task.DeviceTaskOpenPort;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.status.GloryDE50Status;
import devices.glory.task.GloryDE50TaskOperation;
import devices.glory.task.GloryDE50TaskStoreDeposit;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import models.Configuration;
import models.db.LgDevice;
import models.db.LgDevice.DeviceType;
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

        public String sendGloryDE50Operation(GloryDE50OperationInterface operation, final GloryDE50OperationResponse response) {
            return sendGloryDE50Operation(operation, false, response);
        }

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

        public DeviceTaskAbstract poll(int timeoutMS, TimeUnit timeUnit) throws InterruptedException {
            return operationQueue.poll(timeoutMS, timeUnit);
        }

    }

    final GloryDE50DeviceStateApi api = new GloryDE50DeviceStateApi();

    public GloryDE50Device(Enum machineDeviceId, DeviceType deviceType) {
        super(machineDeviceId, deviceType, new SynchronousQueue<DeviceTaskAbstract>());
    }

    @Override
    protected boolean changeProperty(String property, String value) {
        if (property.compareToIgnoreCase("port") == 0) {
            DeviceTaskAbstract deviceTask = new DeviceTaskOpenPort(GloryDE50TaskType.TASK_OPEN_PORT, value);
            if (submit(deviceTask)) {
                return deviceTask.get();
            }
            return false;
        }
        return false;
    }

    private String initialPortValue;

    @Override
    protected void initDeviceProperties(LgDevice lgd) {
        LgDeviceProperty lgSerialPort = LgDeviceProperty.getOrCreateProperty(lgd, "port", LgDeviceProperty.EditType.STRING);
        initialPortValue = lgSerialPort.value;
        lgSerialPort.save();
    }

    @Override
    public DeviceStateInterface init() {
        return new GloryDE50OpenPort(api, initialPortValue);
    }

    @Override
    public void finish() {
        Logger.debug("Executing GotoNeutral command on Stop");
        api.close();
        //   currentCommand = new GotoNeutral(threadCommandApi);
    }

    private boolean runSimpleTask(GloryDE50TaskType st) {
        DeviceTaskAbstract deviceTask = new DeviceTaskAbstract(st);
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean count(List<Integer> slotList) {
        return false;
    }

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
        DeviceTaskAbstract deviceTask = new GloryDE50TaskCount(GloryDE50TaskType.TASK_COUNT, desiredQuantity, currency);
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean envelopeDeposit() {
        return runSimpleTask(GloryDE50TaskType.TASK_ENVELOPE_DEPOSIT);
    }

    public boolean collect() {
        return runSimpleTask(GloryDE50TaskType.TASK_COLLECT);
    }

    public boolean reset() {
        return runSimpleTask(GloryDE50TaskType.TASK_RESET);
    }

    public boolean clearError() {
        return runSimpleTask(GloryDE50TaskType.TASK_CLEAR_ERROR);
    }

    public boolean storingErrorReset() {
        return runSimpleTask(GloryDE50TaskType.TASK_STORING_ERROR_RESET);
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        DeviceTaskAbstract deviceTask = new GloryDE50TaskStoreDeposit(GloryDE50TaskType.TASK_COUNT, sequenceNumber);
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean withdrawDeposit() {
        return runSimpleTask(GloryDE50TaskType.TASK_WITHDRAW_DEPOSIT);
    }

    public GloryDE50TaskOperation sendGloryDE50Operation(OperationWithAckResponse c, boolean b) {
        GloryDE50TaskOperation deviceTask = new GloryDE50TaskOperation(GloryDE50TaskType.TASK_OPERATION, c, b);
        if (submit(deviceTask)) {
            if (deviceTask.get()) {
                return deviceTask;
            }
        }
        return null;
    }

    // normally currentState must not be used outside DeviceAbstract, this is a special exception.
    public boolean cancelDeposit() {
        GloryDE50StateAbstract cs = (GloryDE50StateAbstract) currentState.get();
        return cs.cancelDeposit();
    }
}
