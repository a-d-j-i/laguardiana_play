/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import devices.mei.MeiEbds;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import models.db.LgDevice;
import play.Logger;

/**
 *
 * @author adji
 */
public abstract class Device implements Runnable {

    public enum DeviceType {

        OS_PRINTER() {
                    @Override
                    public Device CreateDevice(String machineDeviceId) {
                        //return new OsPrinter(lgd);
                        //throw new InvalidParameterException();
                        return new MeiEbds(this, machineDeviceId);
                    }
                },
        IO_BOARD_V4520_1_0() {
                    @Override
                    public Device CreateDevice(String machineDeviceId) {
                        //return new OsPrinter(lgd);
                        //throw new InvalidParameterException();
                        return new MeiEbds(this, machineDeviceId);
                    }
                },
        IO_BOARD_V4520_1_2() {
                    @Override
                    public Device CreateDevice(String machineDeviceId) {
                        //return new OsPrinter(lgd);
                        //throw new InvalidParameterException();
                        return new MeiEbds(this, machineDeviceId);
                    }
                },
        IO_BOARD_MX220_1_0() {
                    @Override
                    public Device CreateDevice(String machineDeviceId) {
                        //return new OsPrinter(lgd);
                        //throw new InvalidParameterException();
                        return new MeiEbds(this, machineDeviceId);
                    }
                },
        GLORY_DE50() {
                    @Override
                    public Device CreateDevice(String machineDeviceId) {
                        //return new OsPrinter(lgd);
                        //throw new InvalidParameterException();
                        return new MeiEbds(this, machineDeviceId);
                    }
                },
        MEI_EBDS() {
                    @Override
                    public Device CreateDevice(String machineDeviceId) {
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

        abstract public Device CreateDevice(String machineDeviceId);

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

        public Device CreateDevice() {
            Device ret = deviceType.CreateDevice(machineDeviceId);
            ret.CheckProperties();
            return ret;
        }
    };

    protected final DeviceType deviceType;
    protected final String machineDeviceId;
    protected final LgDevice lgd;

    protected final Thread thread;
    protected final AtomicBoolean mustStop = new AtomicBoolean(false);

    public Device(DeviceType deviceType, String machineDeviceId) {
        lgd = LgDevice.getOrCreateByMachineId(deviceType, machineDeviceId);
        this.deviceType = deviceType;
        this.machineDeviceId = machineDeviceId;
        this.thread = new Thread(this);
    }

    abstract protected void CheckProperties();

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
        while (!mustStop.get()) {
            mainLoop();
        }
        Logger.debug("Device %s thread done", machineDeviceId);
    }

    abstract public void mainLoop();

    abstract public DeviceState getState();

    private Set<DeviceListener> listeners = new HashSet<DeviceListener>();
    //Queue<DeviceEvent> events = new LinkedList<CounterEscrowFullEvent>();

    public void addListener(DeviceListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(DeviceListener listener) {
        this.listeners.remove(listener);
    }

    private void notifyListeners(DeviceState state) {
        for (DeviceListener counterListener : listeners) {
            counterListener.onDeviceEvent(new DeviceEvent(this, state));
        }
    }

    public LgDevice getLgDevice() {
        return lgd;
    }

    @Override
    public String toString() {
        return "Device{" + "deviceType=" + deviceType + ", machineDeviceId=" + machineDeviceId + '}';
    }
}
