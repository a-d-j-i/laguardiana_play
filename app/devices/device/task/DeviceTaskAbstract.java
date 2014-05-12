package devices.device.task;

import devices.device.state.DeviceStateInterface;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author adji
 * @param <T>
 */
abstract public class DeviceTaskAbstract<T> implements DeviceTaskInterface<T> {

    final Lock lock = new ReentrantLock();
    final Condition done = lock.newCondition();
    private T returnValue = null;

    // Executed by the outher thread.
    public T get() {
        lock.lock();
        try {
            done.await();
            return returnValue;
        } catch (InterruptedException ex) {
            return null;
        } finally {
            lock.unlock();
        }
    }

    // Executed by the inner thread.
    public DeviceStateInterface execute(DeviceStateInterface currentState) {
        lock.lock();
        try {
            DeviceStateInterface ret = call(currentState);
            done.signalAll();
            return ret;
        } finally {
            lock.unlock();
        }
    }

    public void setReturnValue(T returnValue) {
        this.returnValue = returnValue;
    }

    abstract protected DeviceStateInterface call(DeviceStateInterface currentState);

}
