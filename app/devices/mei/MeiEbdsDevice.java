package devices.mei;

import devices.device.DeviceAbstract;
import devices.device.DeviceClassCounterIntreface;
import devices.device.DeviceMessageInterface;
import devices.device.DeviceStatusInterface;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceMessageTask;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.state.MeiEbdsOpenPort;
import devices.mei.task.MeiEbdsTaskCount;
import devices.serial.SerialPortReader.DeviceMessageListenerInterface;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import models.db.LgDevice.DeviceType;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public class MeiEbdsDevice extends DeviceAbstract implements DeviceClassCounterIntreface {

    public interface MeiApi extends DeviceMessageListenerInterface {

        public void notifyListeners(DeviceStatusInterface status);

    }

    public enum MeiEbdsTaskType {

        TASK_MESSAGE,
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
    private final MeiEbds mei = new MeiEbds(new MeiApi() {

        public void deviceMessageEvent(DeviceMessageInterface msg) {
            MeiEbdsDevice.this.submit(new DeviceMessageTask(MeiEbdsTaskType.TASK_MESSAGE, msg));
        }

        public void notifyListeners(DeviceStatusInterface status) {
            MeiEbdsDevice.this.notifyListeners(status);
        }

    });

    @Override
    public void finish() {
        Logger.debug("MeiEbds Executing finish");
        Logger.info("MeiEbds Closing mei serial port ");
        mei.close();
    }

    public MeiEbdsDevice(Enum machineDeviceId, DeviceType deviceType) {
        super(machineDeviceId, deviceType);
    }

    @Override
    protected boolean changeProperty(String property, String value) throws InterruptedException, ExecutionException {
        if (property.compareToIgnoreCase("port") == 0) {
            DeviceTaskAbstract deviceTask = new DeviceTaskOpenPort(MeiEbdsTaskType.TASK_OPEN_PORT, value);
            boolean ret = submit(deviceTask).get();
            Logger.debug("changing port to %s %s", value, ret ? "SUCCESS" : "FAIL");
            return ret;
        }
        return false;
    }

    @Override
    public DeviceStateInterface initState() {
        return new MeiEbdsOpenPort(mei);
    }

    public void init() {
        String initialPortValue;
        LgDeviceProperty lgSerialPort = LgDeviceProperty.getOrCreateProperty(lgd, "port", LgDeviceProperty.EditType.STRING);
        initialPortValue = lgSerialPort.value;
        Logger.debug("MeiEbds Executing init");
        submit(new DeviceTaskOpenPort(MeiEbdsTaskType.TASK_OPEN_PORT, initialPortValue));
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

    public boolean count(List<Integer> slotInfo) throws InterruptedException, ExecutionException {
        DeviceTaskAbstract deviceTask = new MeiEbdsTaskCount(MeiEbdsTaskType.TASK_COUNT, slotInfo);
        return submit(deviceTask).get();
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
