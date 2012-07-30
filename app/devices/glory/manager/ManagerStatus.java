/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.Glory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author adji
 */
class ManagerStatus {

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    private final Glory device;
    private final ManagerStatusData data = new ManagerStatusData();

    public class ManagerStatusData {

        private String error = null;
    }

    ManagerStatus( Glory device ) {
        this.device = device;
    }

    Glory getDevice() {
        return device;
    }

    public String getError() {
        r.lock();
        try {
            return data.error;
        } finally {
            r.unlock();
        }
    }

    void setError( String error ) {
        w.lock();
        try {
            data.error = error;
        } finally {
            w.unlock();
        }
    }
}
