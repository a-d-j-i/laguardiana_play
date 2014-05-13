package devices.mei;

import devices.device.DeviceAbstract;
import devices.device.DeviceClassCounterIntreface;
import devices.device.DeviceStatus;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskInterface;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.state.MeiEbdsOpenPort;
import devices.mei.task.MeiEbdsTaskReset;
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
        super(deviceDesc, new ArrayBlockingQueue<DeviceTaskInterface>(1));
        api = new MeiEbdsDeviceStateApi(getOperationQueue());
    }

    @Override
    protected boolean changeProperty(String property, String value) {
        if (property.compareToIgnoreCase("port") == 0) {
            DeviceTaskInterface deviceTask = new DeviceTaskOpenPort(value);
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

    public boolean reset() {
        DeviceTaskInterface deviceTask = new MeiEbdsTaskReset();
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    @Override
    public DeviceStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean clearError() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean count(Map<Integer, Integer> desiredQuantity, Integer currency) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
