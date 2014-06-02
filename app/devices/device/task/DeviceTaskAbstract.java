package devices.device.task;

import devices.device.state.DeviceStateInterface;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author adji
 */
public class DeviceTaskAbstract implements Future<Boolean> {

    final Enum type;
    final Lock lock = new ReentrantLock();
    boolean done = false;
    final Condition cdone = lock.newCondition();
    boolean returnValue = false;

    public DeviceTaskAbstract(Enum type) {
        this.type = type;
    }

    public Boolean get() throws InterruptedException, ExecutionException {
        lock.lock();
        try {
            if (!done) {
                cdone.await();
            }
            return returnValue;
        } finally {
            lock.unlock();
        }
    }

    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        lock.lock();
        try {
            if (!done) {
                cdone.await(timeout, unit);
            }
            return returnValue;
        } finally {
            lock.unlock();
        }
    }

    // Executed by the inner thread.
    public DeviceStateInterface execute(DeviceStateInterface currentState) {
        lock.lock();
        try {
            DeviceStateInterface ret = currentState.call(this);
            done = true;
            cdone.signalAll();
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

    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isCancelled() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isDone() {
        lock.lock();
        try {
            return done;
        } finally {
            lock.unlock();
        }
    }

}
