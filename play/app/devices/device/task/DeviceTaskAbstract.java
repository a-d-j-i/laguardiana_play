package devices.device.task;

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

    final Lock lock = new ReentrantLock();
    boolean done = false;
    final Condition cdone = lock.newCondition();
    boolean returnValue = false;

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

    // called by the inner thread.
    public void setReturnValue(boolean returnValue) {
        lock.lock();
        try {
            this.returnValue = returnValue;
            done = true;
            cdone.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "DeviceTaskAbstract{ done=" + done + ", returnValue=" + returnValue + '}';
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isCancelled() {
        throw new UnsupportedOperationException("Not supported yet.");
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
