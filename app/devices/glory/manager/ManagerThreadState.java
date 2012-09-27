/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.manager.command.ManagerCommandAbstract;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import play.Logger;

/**
 *
 * @author adji
 */
class ManagerThreadState {

    final private Lock mutex = new ReentrantLock();
    final private Condition waitingWait = mutex.newCondition();
    final private Condition commandSent = mutex.newCondition();
    private ManagerCommandAbstract currentCommand = null;
    private boolean mustStop = false;

    class ControllerApi {

        ManagerCommandAbstract getCurrentCommand() {
            mutex.lock();
            try {
                return currentCommand;
            } finally {
                mutex.unlock();
            }
        }

        boolean sendCommand(ManagerCommandAbstract cmd) {
            mutex.lock();
            try {
                if (currentCommand == null) {
                    currentCommand = cmd;
                    commandSent.signal();
                    return true;
                } else {
                    return false;
                }
            } finally {
                mutex.unlock();
            }
        }

        void cancelCommand(Runnable cancelDone) {
            mutex.lock();
            try {
                if (currentCommand != null) {
                    currentCommand.cancel();
                }
            } finally {
                mutex.unlock();
            }
        }

        boolean isWaiting() {
            mutex.lock();
            try {
                return currentCommand == null;
            } finally {
                mutex.unlock();
            }
        }

        void waitUntilWaiting(int timeout) throws InterruptedException {
            waitUntilStop(timeout);
        }
    }

    ControllerApi getControllerApi() {
        return new ControllerApi();
    }

    public class ThreadApi {

        ManagerCommandAbstract getCurrentCommand() {
            ManagerCommandAbstract ret = null;
            mutex.lock();
            try {
                if (currentCommand != null && currentCommand.isDone()) {
                    currentCommand = null;
                }
                if (currentCommand == null) {
                    waitingWait.signalAll();
                    try {
                        commandSent.await();
                    } catch (InterruptedException ex) {
                        Logger.error("getCurrentCommand await interrupted");
                    }
                }
                if (!mustStop) {
                    ret = currentCommand;
                }
            } finally {
                mutex.unlock();
            }
            Logger.debug("getCurrentCommand ret %s", ret);
            return ret;
        }

        boolean mustStop() {
            mutex.lock();
            try {
                return mustStop;
            } finally {
                mutex.unlock();
            }
        }

        void stopped() {
            mutex.lock();
            try {
                waitingWait.signalAll();
            } finally {
                mutex.unlock();
            }
        }
    }

    ThreadApi getThreadApi() {
        return new ThreadApi();
    }

    void stop() {
        mutex.lock();
        try {
            mustStop = true;
            commandSent.signalAll();
        } finally {
            mutex.unlock();
        }
    }

    void waitUntilStop(int timeout) throws InterruptedException {
        mutex.lock();
        try {
            waitingWait.await(timeout, TimeUnit.MILLISECONDS);
        } finally {
            mutex.unlock();
        }
    }
}
