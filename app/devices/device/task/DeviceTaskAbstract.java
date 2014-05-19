package devices.device.task;

import devices.device.state.DeviceStateInterface;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author adji
 */
public class DeviceTaskAbstract {

    final Enum type;
    final Lock lock = new ReentrantLock();
    final Condition done = lock.newCondition();
    boolean returnValue = false;

    public DeviceTaskAbstract(Enum type) {
        this.type = type;
    }

    // Executed by the outher thread.
    public boolean get() {
        lock.lock();
        try {
            done.await();
            return returnValue;
        } catch (InterruptedException ex) {
        } finally {
            lock.unlock();
        }
        return false;
    }

    // Executed by the inner thread.
    public DeviceStateInterface execute(DeviceStateInterface currentState) {
        lock.lock();
        try {
            DeviceStateInterface ret = currentState.call(this);
            done.signalAll();
            return ret;
        } finally {
            lock.unlock();
        }
    }

    public void setReturnValue(boolean returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        return "DeviceTaskAbstract";
    }

    public Enum getType() {
        return type;
    }

}
