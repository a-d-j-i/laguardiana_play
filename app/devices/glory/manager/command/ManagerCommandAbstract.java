/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.GloryStatus.D1Mode;
import devices.glory.GloryStatus.SR1Mode;
import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.Manager.ThreadCommandApi;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class ManagerCommandAbstract implements Runnable {

    static protected class CommandData {

        private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        private final Lock r = rwl.readLock();
        private final Lock w = rwl.writeLock();

        final protected void rlock() {
            r.lock();
        }

        final protected void runlock() {
            r.unlock();
        }

        final protected void wlock() {
            w.lock();
        }

        final protected void wunlock() {
            w.unlock();
        }
    }
    protected final GloryStatus gloryStatus = new GloryStatus();
    protected final ThreadCommandApi threadCommandApi;

    public ManagerCommandAbstract( ThreadCommandApi threadCommandApi ) {
        this.threadCommandApi = threadCommandApi;
    }

    enum CommandState {

        INIT,
        RUN,
        DONE;
    }
    private AtomicReference<CommandState> commandState = new AtomicReference<CommandState>();
    private AtomicBoolean cancel = new AtomicBoolean( false );

    abstract public void execute();

    public void run() {
        commandState.set( CommandState.RUN );
        execute();
        commandState.set( CommandState.DONE );
    }

    public boolean isDone() {
        return commandState.get() == CommandState.DONE;
    }

    public void cancel() {
        cancel.set( true );
    }

    public boolean mustCancel() {
        return cancel.get();
    }

    void gotoNeutral( boolean openEscrow, boolean storingError ) {
        Logger.debug( "GOTO NEUTRAL" );
        if ( !sense() ) {
            return;
        }
        switch ( gloryStatus.getSr1Mode() ) {
            case storing_error:
                if ( !storingError ) {
                    threadCommandApi.setError( "Storing error must call admin" );
                    return;
                }
                switch ( gloryStatus.getD1Mode() ) {
                    case normal_error_recovery_mode:
                    case deposit:
                        if ( !sendGloryCommand( new devices.glory.command.RemoteCancel() ) ) {
                            return;
                        }
                    // dont break;
                    case neutral:
                        storingErrorRecovery();
                        break;
                    default:
                        threadCommandApi.setError( String.format( "Abnormal device Invalid D1 mode %s", gloryStatus.getD1Mode().name() ) );
                        return;
                }
                break;

            case abnormal_device:
                switch ( gloryStatus.getD1Mode() ) {
                    case deposit:
                        if ( !sendGloryCommand( new devices.glory.command.RemoteCancel() ) ) {
                            return;
                        }
                    // dont break;
                    case neutral:
                        if ( !sendGloryCommand( new devices.glory.command.SetErrorRecoveryMode() ) ) {
                            return;
                        }
                        break;
                    case normal_error_recovery_mode:
                        break;
                    default:
                        threadCommandApi.setError( String.format( "Abnormal device Invalid D1 mode %s", gloryStatus.getD1Mode().name() ) );
                        return;
                }
                errorRecovery();
                break;
            default:
                break;
        }

        switch ( gloryStatus.getD1Mode() ) {
            case normal_error_recovery_mode:
            case storing_error_recovery_mode:
            case deposit:
                switch ( gloryStatus.getSr1Mode() ) {
                    case storing_start_request:
                        if ( !openEscrow ) {
                            threadCommandApi.setError( "There are bills in the escrow call an admin" );
                            return;
                        }
                        if ( !sendGloryCommand( new devices.glory.command.OpenEscrow() ) ) {
                            break;
                        }
                    // dont break
                    case escrow_open:
                    case escrow_close_request:
                    case being_restoration:
                        WaitForEmptyEscrow();
                        sendGloryCommand( new devices.glory.command.RemoteCancel() );
                        break;
                    case being_recover_from_storing_error:
                        if ( storingError ) {
                            errorRecovery();
                        } else {
                            WaitForEmptyEscrow();
                            threadCommandApi.setError( "Storing error must call admin" );
                            sendGloryCommand( new devices.glory.command.RemoteCancel() );
                        }
                        break;
                    case counting_start_request:
                        threadCommandApi.setError( "Remove bills from hoper" );
                        return;
                    default:
                        sendGloryCommand( new devices.glory.command.RemoteCancel() );
                        break;
                }
                break;
            case manual:
            case initial:
                if ( !sendGloryCommand( new devices.glory.command.RemoteCancel() ) ) {
                    return;
                }
                if ( gloryStatus.getD1Mode() != GloryStatus.D1Mode.neutral ) {
                    threadCommandApi.setError( String.format( "cant set neutral mode d1 (%s) mode not neutral", gloryStatus.getD1Mode().name() ) );
                }
                break;
            case neutral:
                break;
            default:
                threadCommandApi.setError( String.format( "Invalid D1 mode %s", gloryStatus.getD1Mode().name() ) );
                break;
        }
    }

    boolean sendGloryCommand( GloryCommandAbstract cmd ) {
        if ( cmd != null ) {
            if ( !sendGCommand( cmd ) ) {
                return false;
            }
        }
        return sense();
    }

    boolean sense() {
        if ( !sendGCommand( new devices.glory.command.Sense() ) ) {
            return false;
        }
        Logger.debug( String.format( "D1Mode %s SR1 Mode : %s", gloryStatus.getD1Mode().name(), gloryStatus.getSr1Mode().name() ) );
        return true;
    }

    boolean sendGCommand( GloryCommandAbstract cmd ) {
        if ( cmd == null ) {
            threadCommandApi.setError( "Invalid command null" );
            return false;
        }
        if ( !gloryStatus.setStatusOk( threadCommandApi.sendGloryCommand( cmd ) ) ) {
            String error = gloryStatus.getLastError();
            threadCommandApi.setError( error );
            return false;
        }
        return true;
    }

    void errorRecovery() {
        if ( !sendGloryCommand( new devices.glory.command.ResetDevice() ) ) {
            return;
        }

        while ( !mustCancel() ) {
            Logger.debug( "errorRecovery" );
            if ( !sense() ) {
                return;
            }
            switch ( gloryStatus.getSr1Mode() ) {
                case being_reset:
                case being_restoration:
                    sleep();
                    break;
                default:
                    return;
            }
        }
    }

    void storingErrorRecovery() {
        if ( !sendGloryCommand( new devices.glory.command.SetStroringErrorRecoveryMode() ) ) {
            return;
        }
        waitUntilD1State( D1Mode.storing_error_recovery_mode, true );
        if ( !sendGloryCommand( new devices.glory.command.OpenEscrow() ) ) {
            return;
        }
        // TODO: Review.
        waitUntilSR1State( SR1Mode.being_recover_from_storing_error, true );
        if ( !sendGloryCommand( new devices.glory.command.ResetDevice() ) ) {
            return;
        }
        waitUntilSR1State( SR1Mode.escrow_close_request, true );
        // Close escrow
        WaitForEmptyEscrow();
        if ( !sendGloryCommand( new devices.glory.command.StoringStart( 0 ) ) ) {
            return;
        }
        gotoNeutral( true, false );
    }

    void waitUntilSR1State( SR1Mode state, boolean withCancel ) {
        for ( int i = 0; i < 10; i++ ) {
            Logger.debug( "waitUntilSR1State" );
            if ( withCancel && mustCancel() ) {
                return;
            }
            if ( !sense() ) {
                return;
            }
            SR1Mode m = gloryStatus.getSr1Mode();
            if ( m == state ) {
                return;
            }
            switch ( m ) {
                case abnormal_device:
                    threadCommandApi.setError( String.format( "waitUntilSR1State Abnormal device, todo: get the flags" ) );
                    return;
                case storing_error:
                    threadCommandApi.setError( String.format( "waitUntilSR1State Storing error, todo: get the flags" ) );
                    return;
                default:
                    threadCommandApi.setError( String.format( "waitUntilSR1State invalid sr1 mode %s", gloryStatus.getSr1Mode().name() ) );
                    break;
            }
            sleep();
        }
        threadCommandApi.setError( "waitUntilSR1State waiting too much" );
    }

    void waitUntilD1State( D1Mode state, boolean withCancel ) {
        for ( int i = 0; i < 10; i++ ) {
            Logger.debug( "waitUntilD1State" );
            if ( withCancel && mustCancel() ) {
                return;
            }
            if ( !sense() ) {
                return;
            }
            if ( gloryStatus.getD1Mode() == state ) {
                return;
            }
            switch ( gloryStatus.getSr1Mode() ) {
                case abnormal_device:
                    threadCommandApi.setError( String.format( "waitUntilD1State Abnormal device, todo: get the flags" ) );
                    return;
                case storing_error:
                    threadCommandApi.setError( String.format( "waitUntilD1State Storing error, todo: get the flags" ) );
                    return;
                default:
                    threadCommandApi.setError( String.format( "waitUntilD1State invalid sr1 mode %s", gloryStatus.getSr1Mode().name() ) );
                    break;
            }
            sleep();
        }
        threadCommandApi.setError( "waitUntilD1State waiting too much" );
    }

    void WaitForEmptyEscrow() {
        int i = 0;
        for ( i = 0; i < 0xffff; i++ ) {
            Logger.debug( "WaitForEmptyEscrow" );
            if ( !sense() ) {
                return;
            }
            switch ( gloryStatus.getSr1Mode() ) {
                case being_recover_from_storing_error:
                case escrow_close_request:
                    if ( !sendGloryCommand( new devices.glory.command.CloseEscrow() ) ) {
                        return;
                    }
                    break;
                case being_restoration:
                case escrow_open:
                case escrow_close:
                    break;
                case storing_start_request:
                case waiting:
                    return;
                case abnormal_device:
                    threadCommandApi.setError( String.format( "Abnormal device, todo: get the flags" ) );
                    return;
                case storing_error:
                    threadCommandApi.setError( String.format( "Storing error, todo: get the flags" ) );
                    return;
                default:
                    threadCommandApi.setError( String.format( "WaitForEmptyEscrow invalid sr1 mode %s", gloryStatus.getSr1Mode().name() ) );
                    break;
            }
            sleep();
        }
        threadCommandApi.setError( "WaitForEmptyEscrow waiting too much" );
    }

    void sleep() {
        try {
            Thread.sleep( 1000 );
        } catch ( InterruptedException ex ) {
        }
    }
}
