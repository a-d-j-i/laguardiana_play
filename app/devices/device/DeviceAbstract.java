package devices.device;

import devices.device.state.DeviceStateAbstract;
import devices.device.state.DeviceStateInterface;
import devices.device.status.DeviceStatusInterface;
import devices.device.task.DeviceTaskAbstract;
import java.util.HashSet;
import java.util.Set;
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
        //Logger.debug(message, args);
    }

    abstract public DeviceStateInterface getInitState();

    private DeviceStateInterface currentState;
    final private ExecutorService taskExecutor;

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
            Logger.error("Timeout waiting for task thread : %s", ex.toString());
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
        debug("Device %s Notify listeners : %s", this.toString(), state.toString());
        final DeviceEvent le = new DeviceEvent(this, state);
        for (DeviceEventListener counterListener : listeners) {
            counterListener.onDeviceEvent(le);
        }
    }

    // helper for the inner thread.
    protected void runTask(final DeviceTaskAbstract deviceTask) {
        debug(String.format("------------> %s executing current step: %s with task %s", toString(), currentState, deviceTask.toString()));
        DeviceStateAbstract newState = (DeviceStateAbstract) currentState.call(deviceTask);
        if (newState != null && currentState != newState) {
            while (true) {
                debug("Changing state old %s, new %s", currentState, newState.toString());
                if (newState.isInitialized()) {
                    break;
                }
                DeviceStateAbstract initStateRet = (DeviceStateAbstract) newState.doInit();
                if (initStateRet != null && initStateRet != newState) {
                    newState = initStateRet;
                } else {
                    break;
                }
            }
            debug("setting state to new %s", newState.toString());
            currentState = newState;
        }
        debug("----------------------> Device %s thread done result %s\n\n\n", toString(), deviceTask.toString());
    }

    synchronized public Future<Boolean> submit(final DeviceTaskAbstract deviceTask) {
        taskExecutor.submit(new Runnable() {

            public void run() {
                DeviceAbstract.this.runTask(deviceTask);
            }
        }
        );
        return deviceTask;
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
