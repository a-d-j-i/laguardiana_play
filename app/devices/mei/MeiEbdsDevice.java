package devices.mei;

import devices.device.DeviceAbstract;
import devices.device.DeviceClassCounterIntreface;
import devices.device.DeviceStatusInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.state.MeiEbdsOpenPort;
import devices.mei.task.MeiEbdsTaskCount;
import devices.serial.SerialPortAdapterAbstract;
import devices.serial.SerialPortAdapterInterface;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
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
public class MeiEbdsDevice extends DeviceAbstract implements DeviceClassCounterIntreface {

    public enum MeiEbdsTaskType {

        TASK_OPEN_PORT,
        TASK_RESET,
        TASK_COUNT,
        TASK_STORE, TASK_REJECT,
        TASK_CANCEL;
    }

    public interface MessageSubType {

        public int getId();

    };

    public enum ExtendedMessageSubType implements MessageSubType {

        BarcodeData(0x01),
        RequestSupportedNoteSet(0x02),
        SetExtendedNoteInhibits(0x03),
        SetEscrowTimeouts(0x04);

        private final int id;

        private ExtendedMessageSubType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    };

    public enum MessageType {

        HostToAcceptor(0x10),
        AcceptorToHost(0x20),
        BookmarkSelected(0x30),
        CalibrateMode(0x40),
        FlashDownload(0x50),
        Request(0x60),
        Extended(0x70),
        ENQ(0x100),
        Error(0x1000);

        static {
            for (MessageSubType mt : ExtendedMessageSubType.values()) {
                Extended.msgSubTypeMap.put(mt.getId(), mt);
            }
        }

        final private Map<Integer, MessageSubType> msgSubTypeMap = new HashMap<Integer, MessageSubType>();
        final private int id;

        private MessageType(int id) {
            this.id = id;
        }

        public MessageSubType getSubType(int subtypeId) {
            return msgSubTypeMap.get(subtypeId);
        }

        public int getId() {
            return id;
        }

    };

    public class MeiEbdsDeviceStateApi {

        final private SerialPortAdapterAbstract.PortConfiguration portConf = new SerialPortAdapterAbstract.PortConfiguration(
                SerialPortAdapterAbstract.PORTSPEED.BAUDRATE_9600, SerialPortAdapterAbstract.PORTBITS.BITS_7,
                SerialPortAdapterAbstract.PORTSTOPBITS.STOP_BITS_1, SerialPortAdapterAbstract.PORTPARITY.PARITY_EVEN);
        final private static int MEI_EBDS_READ_TIMEOUT = 10000; //35ms
        final private MeiEbds mei = new MeiEbds(MEI_EBDS_READ_TIMEOUT);

        public boolean open(String value) {
            Logger.debug("api open");
            SerialPortAdapterInterface serialPort = Configuration.getSerialPort(value, portConf);
            Logger.info(String.format("MEI Configuring serial port %s == %s", serialPort.toString(), value));
            mei.close();
            Logger.debug("Mei port open try %s", serialPort.toString());
            boolean ret = mei.open(serialPort);
            Logger.debug("Mei port open : %s", ret ? "success" : "fails");
            return ret;
        }

        void close() {
            mei.close();
        }

        public DeviceTaskAbstract poll(int timeoutMS, TimeUnit timeUnit) throws InterruptedException {
            return operationQueue.poll(timeoutMS, timeUnit);
        }

        public DeviceTaskAbstract poll() {
            return operationQueue.poll();
        }

        public void notifyListeners(DeviceStatusInterface status) {
            MeiEbdsDevice.this.notifyListeners(status);
        }

        public MeiEbds getMei() {
            return mei;
        }

    }

    final MeiEbdsDeviceStateApi api = new MeiEbdsDeviceStateApi();

    public MeiEbdsDevice(Enum machineDeviceId, DeviceType deviceType) {
        super(machineDeviceId, deviceType, new ArrayBlockingQueue<DeviceTaskAbstract>(1));
    }

    @Override
    protected boolean changeProperty(String property, String value) {
        if (property.compareToIgnoreCase("port") == 0) {
            DeviceTaskAbstract deviceTask = new DeviceTaskOpenPort(MeiEbdsTaskType.TASK_OPEN_PORT, value);
            if (submit(deviceTask)) {
                boolean ret = deviceTask.get();
                Logger.debug("changing port to %s %s", value, ret ? "SUCCESS" : "FAIL");
                return ret;
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
        /*        if (lgd.slots == null || lgd.slots.isEmpty()) {
         lgd.slots.add(new LgDeviceSlot());
         }
         */
    }

    @Override
    public DeviceStateInterface init() {
        Logger.debug("MeiEbds Executing init");
        return new MeiEbdsOpenPort(api, initialPortValue);
    }

    @Override
    public void finish() {
        Logger.debug("MeiEbds Executing finish");
        Logger.info("MeiEbds Closing mei serial port ");
        api.close();
    }

    @Override
    protected boolean submitSimpleTask(Enum st) {
        boolean ret = super.submitSimpleTask(st);
        interruptThread();
        return ret;
    }

    @Override
    protected synchronized boolean submit(DeviceTaskAbstract deviceTask) {
        boolean ret = super.submit(deviceTask);
        interruptThread();
        return ret;
    }

    public boolean reset() {
        return submitSimpleTask(MeiEbdsTaskType.TASK_RESET);
    }

    public boolean cancelDeposit() {
        return submitSimpleTask(MeiEbdsTaskType.TASK_CANCEL);
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        return submitSimpleTask(MeiEbdsTaskType.TASK_STORE);
    }

    public boolean withdrawDeposit() {
        return submitSimpleTask(MeiEbdsTaskType.TASK_REJECT);
    }

    public boolean clearError() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean count(List<Integer> slotInfo) {
        DeviceTaskAbstract deviceTask = new MeiEbdsTaskCount(MeiEbdsTaskType.TASK_COUNT, slotInfo);
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
        return false;
    }

    public boolean envelopeDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean collect() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean storingErrorReset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
