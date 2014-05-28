/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.device;

import devices.device.events.DeviceEventListener;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import models.db.LgDevice;
import models.db.LgDevice.DeviceType;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public abstract class DeviceAbstract implements DeviceInterface, Runnable {

    public final Enum machineDeviceId;
//    public final DeviceDescription deviceDescription;
    private final LgDevice lgd;

    private final Thread thread;
    private final AtomicBoolean mustStop = new AtomicBoolean(false);
    protected final BlockingQueue<DeviceTaskAbstract> operationQueue;

    public DeviceAbstract(Enum machineDeviceId, DeviceType deviceType, BlockingQueue<DeviceTaskAbstract> operationQueue) {
        this.operationQueue = operationQueue;
//        this.deviceDescription = deviceDescription;
        this.machineDeviceId = machineDeviceId;
        lgd = LgDevice.getOrCreateByMachineId(machineDeviceId, deviceType);
        this.thread = new Thread(this);
    }

    public void start() {
        Logger.debug("Device %s start", machineDeviceId.name());
        initDeviceProperties(lgd);
        lgd.save();
        Logger.debug("Device %s thread start", machineDeviceId.name());
        thread.start();
        Logger.debug("Device %s start done", machineDeviceId.name());
    }

    public void stop() {
        Logger.debug("Device %s stop", machineDeviceId.name());
        mustStop.set(true);
        thread.interrupt();
        try {
            thread.join(20000);
        } catch (InterruptedException ex) {
            Logger.error("Timeout waiting for thread : %s", ex);
        }
        Logger.debug("Device %s stop done", machineDeviceId.name());
    }

    // Must be touched only by the inner thread !!!
    final protected AtomicReference<DeviceStateInterface> currentState = new AtomicReference<DeviceStateInterface>();

    abstract public DeviceStateInterface init();

    public void run() {
        Logger.debug("Device %s thread started", machineDeviceId.name());
        currentState.set(init());
        while (!mustStop.get()) {
            DeviceStateInterface oldState = currentState.get();

            Logger.debug(String.format("%s executing current step: %s", getClass().getSimpleName(), oldState.toString()));
            DeviceStateInterface newState = oldState.step();
            if (newState != null && oldState != newState) {
                Logger.debug("Changing state old %s, new %s", oldState.toString(), newState.toString());
                DeviceStateInterface initStateRet = newState.init();
                if (initStateRet != null) {
                    newState = initStateRet;
                }
                Logger.debug("setting state to new %s", newState.toString());
                currentState.set(newState);
            }
        }
        finish();
        Logger.debug("Device %s thread done", machineDeviceId.name());
    }

    public void finish() {
    }

    private final Set<DeviceEventListener> listeners = new HashSet<DeviceEventListener>();
    //Queue<DeviceEvent> events = new LinkedList<CounterEscrowFullEvent>();

    public void addEventListener(DeviceEventListener listener) {
        this.listeners.add(listener);
    }

    public void removeEventListener(DeviceEventListener listener) {
        this.listeners.remove(listener);
    }

    final private AtomicReference<DeviceEvent> lastEvent = new AtomicReference<DeviceEvent>();

    protected void notifyListeners(DeviceStatusInterface state) {
        final DeviceEvent le = new DeviceEvent(this, state);
        lastEvent.set(le);
        for (DeviceEventListener counterListener : listeners) {
            counterListener.onDeviceEvent(le);
        }
    }

    public DeviceEvent getLastEvent() {
        return lastEvent.get();
    }

    public Integer getDeviceId() {
        return lgd.deviceId;
    }

    public DeviceType getType() {
        return lgd.deviceType;
    }

    public Enum getMachineDeviceId() {
        return machineDeviceId;
    }

    public List<LgDeviceProperty> getEditableProperties() {
        return LgDeviceProperty.getEditables(lgd);
    }

    protected abstract boolean changeProperty(String property, String value);

    abstract protected void initDeviceProperties(LgDevice lgd);

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

    synchronized protected boolean submit(DeviceTaskAbstract deviceTask) {
        return operationQueue.offer(deviceTask);
    }

    protected boolean submitSimpleTask(Enum st) {
        DeviceTaskAbstract deviceTask = new DeviceTaskAbstract(st);
        if (submit(deviceTask)) {
            return deviceTask.get();
        }
        return false;
    }

    @Override
    public String toString() {
        return "Device{ deviceID = " + getDeviceId() + ", machineDeviceId=" + machineDeviceId.name() + '}';
    }

}
