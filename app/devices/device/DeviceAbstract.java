package devices.device;

import devices.device.events.DeviceEventListener;
import devices.device.state.DeviceStateAbstract;
import devices.device.state.DeviceStateInterface;
import devices.device.task.DeviceTaskAbstract;
import devices.device.task.DeviceTaskOpenPort;
import devices.mei.task.MeiEbdsTaskCount;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import models.db.LgDevice;
import models.db.LgDevice.DeviceType;
import models.db.LgDeviceProperty;
import play.Logger;

/**
 *
 * @author adji
 */
public abstract class DeviceAbstract implements DeviceInterface {

    final public Enum machineDeviceId;
    final protected LgDevice lgd;
    final private ExecutorService taskExecutor;

    public DeviceAbstract(final Enum machineDeviceId, DeviceType deviceType) {
        this.machineDeviceId = machineDeviceId;
        lgd = LgDevice.getOrCreateByMachineId(machineDeviceId, deviceType);
        this.taskExecutor = Executors.newSingleThreadExecutor();
    }

    // TODO: possible race condition on start.
    public void start() {
        Logger.debug("Device %s start", machineDeviceId.name());
        currentState = initState();
        Logger.debug("Device %s start done", machineDeviceId.name());
        init();
    }

    abstract public DeviceStateInterface initState();

    abstract public void init();

    public void stop() {
        Logger.debug("Device %s stop task thread", machineDeviceId.name());
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
        Logger.debug("Device %s stop done", machineDeviceId.name());
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

    public DeviceType getType() {
        return lgd.deviceType;
    }

    public Enum getMachineDeviceId() {
        return machineDeviceId;
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
        return taskExecutor.submit(new Callable<Boolean>() {

            public Boolean call() throws Exception {
                Logger.debug(String.format("%s executing current step: %s", machineDeviceId.name(), currentState));
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
                Logger.debug("Device %s thread done", machineDeviceId.name());
                return true;
            }
        }
        );
    }

    protected boolean submitSimpleTask(Enum st) {
        DeviceTaskAbstract deviceTask = new DeviceTaskAbstract(st);
        try {
            return submit(deviceTask).get();
        } catch (InterruptedException ex) {
            Logger.error("exception in change property %s", ex.toString());
        } catch (ExecutionException ex) {
            Logger.error("exception in change property %s", ex.toString());
        }
        return false;
    }

    @Override
    public String toString() {
        return "Device{ deviceID = " + getDeviceId() + ", machineDeviceId=" + machineDeviceId.name() + '}';
    }

}
