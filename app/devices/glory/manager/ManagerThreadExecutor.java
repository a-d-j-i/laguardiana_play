/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.manager.Manager.ManagerCommand;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import play.Logger;

/**
 * TODO: Use java.util.concurrent interfaces and correct usage of Thread.
 *
 * @author adji
 */
class ManagerThreadExecutor {

    final private ManagerThread thread;

    ManagerThreadExecutor( ManagerStatus status ) {
        thread = new ManagerThread( this, status );
    }

    void start() {
        thread.start();
    }

    void join( int timeout ) throws InterruptedException {
        thread.join( timeout );
    }

    public void stop() {
        mutex.lock();
        try {
            Logger.debug( "----- Thread Executor STOP" );
            thState = ThState.STOP;
        } finally {
            mutex.unlock();
        }
    }

    public enum ThState {

        WAITING,
        COMMAND_SENT,
        PROCESSING,
        STOP,
        STOPPED,}
    final private Lock mutex = new ReentrantLock();
    final private Condition canceled = mutex.newCondition();
    final private Condition commandSent = mutex.newCondition();
    private ThState thState = ThState.WAITING;
    private ManagerCommand currentCommand = ManagerCommand.NONE;

    public boolean sendCommand( ManagerCommand cmd ) {
        mutex.lock();
        try {
            if ( thState == ThState.WAITING ) {
                thState = ThState.COMMAND_SENT;
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

    void cancelLastCommand() {
        mutex.lock();
        try {
            switch ( thState ) {
                case WAITING:
                    break;
                case COMMAND_SENT:
                    currentCommand = null;
                    thState = ThState.WAITING;
                    break;
                case PROCESSING:
                    thState = ThState.STOP;
                    try {
                        canceled.await();
                    } catch ( InterruptedException ex ) {
                        Logger.error( "Interrupt in cancelLastCommand" );
                    }
                    break;
                case STOP:
                case STOPPED:
                    break;
                default:
                    Logger.error( "Invalid state in cancelLastCommand %s", thState.name() );
                    break;
            }
        } finally {
            mutex.unlock();
        }
    }

    public ManagerCommand getNextCommand() {
        ManagerCommand ret = ManagerCommand.NONE;
        mutex.lock();
        Logger.debug( String.format( "getNextCommand Executor state %s", thState.name() ) );
        try {
            switch ( thState ) {
                case WAITING:
                    try {
                        commandSent.await();
                    } catch ( InterruptedException ex ) {
                        Logger.error( "Interrupt in manager thread" );
                    }
                    break;
                case COMMAND_SENT:
                    ret = currentCommand;
                    thState = ThState.PROCESSING;
                    break;
                case PROCESSING:
                    canceled.signalAll();
                    thState = ThState.WAITING;
                    break;
                case STOP:
                    thState = ThState.STOPPED;
                    break;
                case STOPPED:
                default:
                    // this must never happned
                    Logger.error( "CommandExecutor state error %s", thState.name() );
                    //avoid a fast loop.
                    try {
                        commandSent.await( 1, TimeUnit.SECONDS );
                    } catch ( InterruptedException ex ) {
                        Logger.error( "Interrupt in manager thread 2" );
                    }
                    break;
            }
        } finally {
            mutex.unlock();
        }
        return ret;
    }

    public boolean mustStop() {
        mutex.lock();
        try {
            if ( thState == ThState.STOP || thState == ThState.STOPPED ) {
                return true;
            }
        } finally {
            mutex.unlock();
        }
        return false;
    }
}
