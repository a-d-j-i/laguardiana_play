package devices.mei;

import devices.device.DeviceAbstract;
import devices.device.DeviceClassCounterIntreface;
import devices.device.DeviceStatus;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.state.MeiEbdsOpenPort;
import devices.mei.task.MeiEbdsTaskCount;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import machines.Machine;
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
        TASK_STORE,
        TASK_CANCEL;
    }

    public enum MessageType {

        HostToAcceptor(0x10),
        AcceptorToHost(0x20),
        BookmarkSelected(0x30),
        CalibrateMode(0x40),
        FlashDownload(0x50),
        Request(0x60),
        Extended(0x70);
        private final int id;

        private MessageType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

    };

    final MeiEbdsDeviceStateApi api;

    public MeiEbdsDevice(Machine.DeviceDescription deviceDesc) {
        super(deviceDesc, new ArrayBlockingQueue<DeviceTaskAbstract>(1));
        api = new MeiEbdsDeviceStateApi(operationQueue);
    }

    @Override
    protected boolean changeProperty(String property, String value) {
        if (property.compareToIgnoreCase("port") == 0) {
            DeviceTaskAbstract deviceTask = new DeviceTaskOpenPort(MeiEbdsTaskType.TASK_OPEN_PORT, value);
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

    private boolean runSimpleTask(MeiEbdsTaskType st) {
        DeviceTaskAbstract deviceTask = new DeviceTaskAbstract(st);
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    public boolean reset() {
        return runSimpleTask(MeiEbdsTaskType.TASK_RESET);
    }

    public boolean store() {
        return false;
    }

    @Override
    public DeviceStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    public boolean cancelDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean storeDeposit(Integer sequenceNumber) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean withdrawDeposit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Integer getCurrency() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Map<Integer, Integer> getCurrentQuantity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Map<Integer, Integer> getDesiredQuantity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
