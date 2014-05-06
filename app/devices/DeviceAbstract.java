/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import devices.mei.MeiEbds;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import models.db.LgDevice;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public abstract class DeviceAbstract implements DeviceInterface, Runnable {

    public enum DeviceType {

        OS_PRINTER() {
                    @Override
                    public DeviceAbstract createDevice(String machineDeviceId) {
                        //return new OsPrinter(lgd);
                        //throw new InvalidParameterException();
                        return new MeiEbds(this, machineDeviceId);
                    }
                },
        IO_BOARD_V4520_1_0() {
                    @Override
                    public DeviceAbstract createDevice(String machineDeviceId) {
                        //return new OsPrinter(lgd);
                        //throw new InvalidParameterException();
                        return new MeiEbds(this, machineDeviceId);
                    }
                },
        IO_BOARD_V4520_1_2() {
                    @Override
                    public DeviceAbstract createDevice(String machineDeviceId) {
                        //return new OsPrinter(lgd);
                        //throw new InvalidParameterException();
                        return new MeiEbds(this, machineDeviceId);
                    }
                },
        IO_BOARD_MX220_1_0() {
                    @Override
                    public DeviceAbstract createDevice(String machineDeviceId) {
                        //return new OsPrinter(lgd);
                        //throw new InvalidParameterException();
                        return new MeiEbds(this, machineDeviceId);
                    }
                },
        GLORY_DE50() {
                    @Override
                    public DeviceAbstract createDevice(String machineDeviceId) {
                        //return new OsPrinter(lgd);
                        //throw new InvalidParameterException();
                        return new MeiEbds(this, machineDeviceId);
                    }
                },
        MEI_EBDS() {
                    @Override
                    public DeviceAbstract createDevice(String machineDeviceId) {
                        //return new OsPrinter(lgd);
                        //throw new InvalidParameterException();
                        return new MeiEbds(this, machineDeviceId);
                    }
                };

        public enum DeviceClass {

            DEVICE_CLASS_PRINTER,
            DEVICE_CLASS_COUNTER,
            DEVICE_CLASS_IOBOARD,
        };

        abstract public DeviceAbstract createDevice(String machineDeviceId);

        @Override
        public String toString() {
            return name();
        }

    };

    static public class DeviceDesc {

        DeviceType deviceType;
        String machineDeviceId;

        public DeviceDesc(DeviceType deviceType, String machineDeviceId) {
            this.deviceType = deviceType;
            this.machineDeviceId = machineDeviceId;
        }

        @Override
        public String toString() {
            return "deviceType=" + deviceType + ", machineDeviceId=" + machineDeviceId;
        }

        public DeviceAbstract createDevice() {
            DeviceAbstract ret = deviceType.createDevice(machineDeviceId);
            ret.initDeviceProperties();
            return ret;
        }
    };

    protected final DeviceType deviceType;
    protected final String machineDeviceId;
    protected final LgDevice lgd;

    protected final Thread thread;
    protected final AtomicBoolean mustStop = new AtomicBoolean(false);

    public DeviceAbstract(DeviceType deviceType, String machineDeviceId) {
        lgd = LgDevice.getOrCreateByMachineId(deviceType, machineDeviceId);
        this.deviceType = deviceType;
        this.machineDeviceId = machineDeviceId;
        this.thread = new Thread(this);
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        mustStop.set(true);
        try {
            thread.join(10000);
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    public void run() {
        Logger.debug("Device %s thread started", machineDeviceId);
        assemble();
        while (!mustStop.get()) {
            mainLoop();
        }
        disassemble();
        Logger.debug("Device %s thread done", machineDeviceId);
    }

    abstract public void mainLoop();

    public void assemble() {
    }

    public void disassemble() {
    }

    abstract public DeviceStatus getStatus();

    private Set<DeviceEventListener> listeners = new HashSet<DeviceEventListener>();
    //Queue<DeviceEvent> events = new LinkedList<CounterEscrowFullEvent>();

    public void addEventListener(DeviceEventListener listener) {
        this.listeners.add(listener);
    }

    public void removeEventListener(DeviceEventListener listener) {
        this.listeners.remove(listener);
    }

    protected void notifyListeners(DeviceStatus state) {
        for (DeviceEventListener counterListener : listeners) {
            counterListener.onDeviceEvent(new DeviceEvent(this, state));
        }
    }

    public String getName() {
        return lgd.deviceType.name();
    }

    public Integer getDeviceId() {
        return lgd.deviceId;
    }

    public List<LgDeviceProperty> getEditableProperties() {
        return LgDeviceProperty.getEditables(lgd);
    }

    protected abstract void changeProperty(LgDeviceProperty lgdp);

    abstract protected void initDeviceProperties();

    public LgDeviceProperty setProperty(String property, String value) {
        LgDeviceProperty l = LgDeviceProperty.setOrCreateProperty(lgd, property, value);
        if (l != null) {
            changeProperty(l);

        }
        return l;
    }

    @Override
    public String toString() {
        return "Device{" + "deviceType=" + deviceType + ", machineDeviceId=" + machineDeviceId + '}';
    }
}
