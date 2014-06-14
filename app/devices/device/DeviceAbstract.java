package devices.device;

import devices.device.events.DeviceEventListener;
import devices.device.state.DeviceStateInterface;
import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskAbstract;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import models.db.LgDevice;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public abstract class DeviceAbstract implements DeviceInterface {

    final public String machineDeviceId;
    final protected LgDevice lgd;
    final private ExecutorService taskExecutor;

    public DeviceAbstract(final String machineDeviceId, LgDevice.DeviceType deviceType) {
        this.machineDeviceId = machineDeviceId;
        lgd = LgDevice.getOrCreateByMachineId(machineDeviceId, deviceType);
        this.taskExecutor = Executors.newSingleThreadExecutor();
    }

    // TODO: possible race condition on start.
    public void start() {
        Logger.debug("Device %s start", machineDeviceId);
        currentState = initState();
        Logger.debug("Device %s start done", machineDeviceId);
        init();
    }

    abstract public DeviceStateInterface initState();

    abstract public void init();

    public void stop() {
        Logger.debug("Device %s stop task thread", machineDeviceId);
        taskExecutor.shutdown();
        try {
            /*        taskThread.interrupt();
             try {
             taskThread.join(20000);
             } catch (InterruptedException ex) {
             Logger.error("Timeout waiting for task thread : %s", ex);
             }*/
            taskExecutor.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.error("Timeout waiting for task thread : %s", ex);
        }
        finish();
        Logger.debug("Device %s stop done", machineDeviceId);
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

    // Just for logging proposes.
    final private BlockingQueue<DeviceEvent> eventHistory = new ArrayBlockingQueue<DeviceEvent>(10);

    protected void notifyListeners(DeviceStatusInterface state) {
        final DeviceEvent le = new DeviceEvent(this, state);
        eventHistory.offer(le);
        for (DeviceEventListener counterListener : listeners) {
            counterListener.onDeviceEvent(le);
        }
    }

    public DeviceEvent getLastEvent() {
        return eventHistory.poll();
    }

    public Integer getDeviceId() {
        return lgd.deviceId;
    }

    public String getMachineDeviceId() {
        return machineDeviceId;
    }

    public Enum getType() {
        return lgd.deviceType;
    }

    public List<LgDeviceProperty> getEditableProperties() {
        return LgDeviceProperty.getEditables(lgd);
    }

    protected abstract boolean changeProperty(String property, String value) throws InterruptedException, ExecutionException;

    public LgDeviceProperty setProperty(String property, String value) throws InterruptedException, ExecutionException {
        LgDeviceProperty l = LgDeviceProperty.getProperty(lgd, property);
        if (l != null) {
            if (changeProperty(property, value)) {
                l.value = value;
                l.save();
            }
        }
        return l;
    }

    protected DeviceStateInterface currentState;

    synchronized protected Future<Boolean> submit(final DeviceTaskAbstract deviceTask) {
        Logger.debug("---> Submitting task : " + deviceTask.toString());
        return taskExecutor.submit(new Callable<Boolean>() {

            public Boolean call() throws Exception {
                Logger.debug(String.format("%s executing current step: %s", machineDeviceId, currentState));
                DeviceStateInterface newState = deviceTask.execute(currentState);
                if (newState != null && currentState != newState) {
                    Logger.debug("Changing state old %s, new %s", currentState, newState.toString());
                    DeviceStateInterface initStateRet = newState.init();
                    if (initStateRet != null) {
                        newState = initStateRet;
                    }
                    Logger.debug("setting state to new %s", newState.toString());
                    currentState = newState;
                }
                Logger.debug("Device %s thread done", machineDeviceId);
                return deviceTask.get();
            }
        }
        );
    }

    protected Future<Boolean> submitSimpleTask(Enum st) {
        return submit(new DeviceTaskAbstract(st));
    }

    protected Future<Boolean> falseFuture(final boolean ret) {
        return new Future<Boolean>() {

            public boolean cancel(boolean mayInterruptIfRunning) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public boolean isCancelled() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public boolean isDone() {
                return true;
            }

            public Boolean get() throws InterruptedException, ExecutionException {
                return ret;
            }

            public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return ret;
            }
        };
    }

    @Override
    public String toString() {
        return "Device{ deviceID = " + getDeviceId() + ", machineDeviceId=" + machineDeviceId + '}';
    }

}
