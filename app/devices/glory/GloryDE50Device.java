package devices.glory;

import devices.device.DeviceAbstract;
import devices.device.DeviceClassCounterIntreface;
import devices.device.DeviceStatus;
import devices.device.state.DeviceStateAbstract;
import devices.device.task.DeviceTaskInterface;
import devices.device.task.DeviceTaskOperation;
import devices.glory.operation.GloryDE50OperationInterface;
import devices.glory.operation.OperationWithAckResponse;
import devices.glory.response.GloryDE50OperationResponse;
import devices.glory.state.GloryDE50StateAbstract;
import devices.glory.state.OpenPort;
import devices.glory.task.GloryDE50TaskCollect;
import devices.glory.task.GloryDE50TaskCount;
import devices.glory.task.GloryDE50TaskEnvelopeDeposit;
import devices.glory.task.GloryDE50TaskOpenPort;
import devices.glory.task.GloryDE50TaskReset;
import devices.glory.task.GloryDE50TaskStoreDeposit;
import devices.glory.task.GloryDE50TaskWithdraw;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import machines.Machine;
import models.Configuration;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public class GloryDE50Device extends DeviceAbstract implements DeviceClassCounterIntreface {

    public class GloryDE50StateApi extends DeviceAbstract.DeviceStateApi {

        final private SerialPortAdapterAbstract.PortConfiguration portConf = new SerialPortAdapterAbstract.PortConfiguration(SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7, SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN);

        boolean closing = false;

        public GloryDE50OperationResponse sendGloryDE50Operation(GloryDE50OperationInterface operation) {
            return sendGloryDE50Operation(operation, false);
        }

        public GloryDE50OperationResponse sendGloryDE50Operation(GloryDE50OperationInterface operation, boolean debug) {
            return gl.sendOperation(operation, debug);
        }

        public void notifyListeners(String details) {
        }

        public void notifyListeners(DeviceStatus.STATUS status) {
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

    }
    static final private int GLORY_READ_TIMEOUT = 5000;
    static final private GloryDE50 gl = new GloryDE50(GLORY_READ_TIMEOUT);
    final private GloryDE50StateApi api = new GloryDE50StateApi();
    DeviceStatus currentStatus;

    public GloryDE50Device(Machine.DeviceDescription deviceDesc) {
        super(deviceDesc, new SynchronousQueue<DeviceTaskInterface>());
    }

    @Override
    protected boolean changeProperty(String property, String value) {
        if (property.compareToIgnoreCase("port") == 0) {
            DeviceTaskInterface<Boolean> deviceTask = new GloryDE50TaskOpenPort(value);
            if (submit(deviceTask)) {
                return deviceTask.get();
            }
            return false;
        }
        return false;
    }

    private String initialPortValue;

    @Override
    protected void initDeviceProperties() {
        LgDeviceProperty lgSerialPort = LgDeviceProperty.getOrCreateProperty(lgd, "port", LgDeviceProperty.EditType.STRING);
        initialPortValue = lgSerialPort.value;
    }

    @Override
    public DeviceStateAbstract init() {
        return new OpenPort(new GloryDE50StateApi(), initialPortValue);
    }

    @Override
    public void finish() {
        Logger.debug("Executing GotoNeutral command on Stop");
        gl.close();
        //   currentCommand = new GotoNeutral(threadCommandApi);
    }

    public boolean count(final Map<Integer, Integer> desiredQuantity, final Integer currency) {
        DeviceTaskInterface<Boolean> deviceTask = new GloryDE50TaskCount(desiredQuantity, currency);
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean envelopeDeposit() {
        DeviceTaskInterface<Boolean> deviceTask = new GloryDE50TaskEnvelopeDeposit();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean collect() {
        DeviceTaskInterface<Boolean> deviceTask = new GloryDE50TaskCollect();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean reset() {
        DeviceTaskInterface<Boolean> deviceTask = new GloryDE50TaskReset();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean storingErrorReset() {
        DeviceTaskInterface<Boolean> deviceTask = new GloryDE50TaskReset();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        DeviceTaskInterface<Boolean> deviceTask = new GloryDE50TaskStoreDeposit();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean withdrawDeposit() {
        DeviceTaskInterface<Boolean> deviceTask = new GloryDE50TaskWithdraw();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public GloryDE50OperationResponse sendGloryDE50Operation(OperationWithAckResponse c, boolean b) {
        DeviceTaskOperation deviceTask = new DeviceTaskOperation(c, b);
        if (submit(deviceTask)) {
            return (GloryDE50OperationResponse) deviceTask.get();
        }
        return null;
    }

    @Override
    public GloryDE50StateAbstract getCurrentState() {
        return (GloryDE50StateAbstract) super.getCurrentState();
    }

    public boolean cancelDeposit() {
        return getCurrentState().cancelDeposit();
    }

    public Integer getCurrency() {
        return getCurrentState().getCurrency();
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        return getCurrentState().getCurrentQuantity();
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        return getCurrentState().getDesiredQuantity();
    }

    public boolean clearError() {
        return getCurrentState().clearError();
    }

    @Override
    public DeviceStatus getStatus() {
        GloryDE50StateAbstract st = getCurrentState();
        return new DeviceStatus(st.getError());
    }
}
