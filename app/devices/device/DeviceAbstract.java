package devices.device;

import devices.device.state.DeviceStateInterface;
import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskAbstract;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import play.Logger;

/**
 *
 * @author adji
 */
public abstract class DeviceAbstract implements DeviceInterface {

    private void debug(String message, Object... args) {
        Logger.debug(message, args);
    }

    abstract public DeviceStateInterface getInitState();

    private DeviceStateInterface currentState;
    final ExecutorService taskExecutor;

    public DeviceAbstract() {
        this.taskExecutor = Executors.newSingleThreadExecutor();
    }

    public void start() {
        currentState = getInitState();
    }

    public void stop() {
        debug("Device %s stop task thread", this.toString());
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
        debug("Device %s stop done", this.toString());
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

    public void notifyListeners(DeviceStatusInterface state) {
        final DeviceEvent le = new DeviceEvent(this, state);
        for (DeviceEventListener counterListener : listeners) {
            counterListener.onDeviceEvent(le);
        }
    }

    protected boolean runTask(final DeviceTaskAbstract deviceTask) {
        debug(String.format("%s executing current step: %s", DeviceAbstract.this.toString(), currentState));
        DeviceStateInterface newState = deviceTask.execute(currentState);
        if (newState != null && currentState != newState) {
            debug("Changing state old %s, new %s", currentState, newState.toString());
            DeviceStateInterface initStateRet = newState.init();
            if (initStateRet != null) {
                newState = initStateRet;
            }
            debug("setting state to new %s", newState.toString());
            currentState = newState;
        }
        debug("Device %s thread done", DeviceAbstract.this.toString());
        try {
            boolean ret = deviceTask.get();
            debug("Device %s thread got %s", DeviceAbstract.this.toString(), ret ? "TRUE" : "FALSE");
            return ret;
        } catch (InterruptedException ex) {
            Logger.error("InterruptedException %s on %s ", ex, DeviceAbstract.this.toString());
        } catch (ExecutionException ex) {
            Logger.error("InterruptedException %s on %s ", ex, DeviceAbstract.this.toString());
        }
        return false;
    }

    synchronized public Future<Boolean> submit(final DeviceTaskAbstract deviceTask) {
        debug("%s ---> Submitting task : %s", this.toString(), deviceTask.toString());
        return taskExecutor.submit(new Callable<Boolean>() {

            public Boolean call() {
                return DeviceAbstract.this.runTask(deviceTask);
            }
        }
        );
    }

    public boolean submitSynchronous(final DeviceTaskAbstract deviceTask) {
        try {
            return submit(deviceTask).get();
        } catch (InterruptedException ex) {
            Logger.error("exception in submitSynchronous", ex.toString());
            ex.printStackTrace();
        } catch (ExecutionException ex) {
            Logger.error("exception in submitSynchronous", ex.toString());
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return "DeviceAbstract";
    }

}
