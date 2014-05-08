/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import devices.events.DeviceEventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import machines.Machine.DeviceDescription;
import models.db.LgDevice;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public abstract class DeviceAbstract implements DeviceInterface, Runnable {

    protected final DeviceDescription deviceDescription;
    protected final LgDevice lgd;

    protected final Thread thread;
    protected final AtomicBoolean mustStop = new AtomicBoolean(false);

    public DeviceAbstract(DeviceDescription deviceDescription) {
        this.deviceDescription = deviceDescription;
        lgd = LgDevice.getOrCreateByMachineId(deviceDescription.getType(), deviceDescription.getMachineId());
        this.thread = new Thread(this);
    }

    public void start() {
        initDeviceProperties();
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
        Logger.debug("Device %s thread started", deviceDescription);
        assemble();
        while (!mustStop.get()) {
            mainLoop();
        }
        disassemble();
        Logger.debug("Device %s thread done", deviceDescription);
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

    public DeviceDescription getDeviceDesc() {
        return deviceDescription;
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

    protected abstract boolean changeProperty(String property, String value);

    abstract protected void initDeviceProperties();

    public LgDeviceProperty setProperty(String property, String value) {
        LgDeviceProperty l = LgDeviceProperty.getProperty(lgd, property);
        if (l != null) {
            if (changeProperty(property, value)) {
                l.value = value;
                l.save();
            }
        }
        return l;
    }

    @Override
    public String toString() {
        return "Device{ deviceID = " + getDeviceId() + ", deviceDesc=" + deviceDescription + '}';
    }
}
