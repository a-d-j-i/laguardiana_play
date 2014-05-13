/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.device;

import devices.device.events.DeviceEventListener;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskInterface;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import machines.Machine.DeviceDescription;
import models.db.LgDevice;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public abstract class DeviceAbstract implements DeviceInterface, Runnable {

    public final DeviceDescription deviceDescription;
    protected final LgDevice lgd;

    private final Thread thread;
    private final AtomicBoolean mustStop = new AtomicBoolean(false);
    private final BlockingQueue<DeviceTaskInterface> operationQueue;

    public DeviceAbstract(DeviceDescription deviceDescription, BlockingQueue<DeviceTaskInterface> operationQueue) {
        this.operationQueue = operationQueue;
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
            Logger.error("Timeout waiting for thread : %s", ex);
        }
    }

    // Must be touched only by the inner thread !!!
    final private AtomicReference<DeviceStateInterface> currentState = new AtomicReference<DeviceStateInterface>();

    abstract public DeviceStateInterface init();

    public void run() {
        Logger.debug("Device %s thread started", deviceDescription);
        currentState.set(init());
        while (!mustStop.get()) {
            DeviceStateInterface oldState = currentState.get();

            Logger.debug(String.format("%s executing current step: %s", getClass().getSimpleName(), oldState.getClass().getSimpleName()));
            DeviceStateInterface newState = oldState.step();

            if (newState != null && oldState != newState) {
                DeviceStateInterface initStateRet = newState.init();
                if (initStateRet != null) {
                    newState = initStateRet;
                }
                currentState.set(newState);
            }
        }
        finish();
        Logger.debug("Device %s thread done", deviceDescription);
    }

    public void finish() {
    }

    public BlockingQueue<DeviceTaskInterface> getOperationQueue() {
        return operationQueue;
    }

    public DeviceStateInterface getCurrentState() {
        return currentState.get();
    }

    abstract public DeviceStatus getStatus();

    private final Set<DeviceEventListener> listeners = new HashSet<DeviceEventListener>();
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

    synchronized protected boolean submit(DeviceTaskInterface deviceTask) {
        return operationQueue.offer(deviceTask);
    }

    @Override
    public String toString() {
        return "Device{ deviceID = " + getDeviceId() + ", deviceDesc=" + deviceDescription + '}';
    }

}
