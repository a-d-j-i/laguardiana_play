/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.manager.script.GoToNeutral;
import devices.glory.GloryStatus;
import devices.glory.manager.Manager.ManagerCommand;
import devices.glory.manager.script.ProcessCmd;
import devices.glory.manager.script.ProcessCount;
import devices.glory.manager.script.WaitForEmptyEscrow;
import play.Logger;

/**
 * TODO: Extend from job, implemente executor....
 *
 * @author adji
 */
public class ManagerThread extends Thread {

    public enum MState {

        INIT,
        WAIT,
        ERROR,
        COUNT,
        CANCEL_EMPTY_ESCROW,
        CANCEL_WAIT_FOR_EMPTY_ESCROW,;
    };
    final private ManagerThreadExecutor executor;
    final private ManagerStatus status;
    final private ManagerThreadGloryHelper helper;
    private MState state = MState.INIT;

    ManagerThread( ManagerThreadExecutor executor, ManagerStatus status ) {
        this.executor = executor;
        this.status = status;
        this.helper = new ManagerThreadGloryHelper( status.getDevice() );
    }

    @Override
    public void run() {
        int[] bills = new int[ 32 ];
        for ( int i = 0; i < bills.length; i++ ) {
            bills[ i] = 0;
        }
        while ( !executor.mustStop() ) {
            Logger.debug( String.format( "Manager state : %s", state.name() ) );
            String error = null;
            switch ( state ) {
                case ERROR:
                case INIT:
                    error = GoToNeutral.execute( this );
                    break;
                case WAIT:
                    error = ProcessCmd.execute( this, executor.getNextCommand() );
                    break;
                case COUNT:
                    error = ProcessCount.execute( this );
                    break;
                case CANCEL_EMPTY_ESCROW:
                    if ( !helper.sendGloryCommand( new devices.glory.command.OpenEscrow() ) ) {
                        error = helper.getStatus().getLastError();
                        break;
                    }
                    state = MState.CANCEL_WAIT_FOR_EMPTY_ESCROW;
                    break;
                case CANCEL_WAIT_FOR_EMPTY_ESCROW:
                    error = WaitForEmptyEscrow.execute( this );
                    break;
                default:
                    error = String.format( "ManagerThread invalid state %s", state.name() );
                    break;

            }
            if ( error != null ) {
                Logger.error( String.format( "In ManagerThread error : %s", error ) );
                status.setError( error );
                state = MState.ERROR;
            }

        }
    }

    private String processCmd( ManagerCommand cmd ) {
        if ( cmd == null ) {
            return "Invalid cmd null";
        }
        Logger.debug( "Get Next Command %s", cmd.name() );

        switch ( cmd ) {
            case START_DEPOSIT:
                if ( !helper.sendGloryCommand( new devices.glory.command.SetDepositMode() ) ) {
                    return helper.getError();
                }
                if ( !helper.sense() ) {
                    return helper.getError();
                }
                if ( helper.getStatus().getD1Mode() != GloryStatus.D1Mode.deposit ) {
                    return String.format( "cant set deposit mode d1 (%s) mode not neutral", helper.getStatus().getD1Mode().name() );
                }
                state = MState.COUNT;
                break;
            case STOP:
                // Close escrow, etc, etc...
                break;
            case NONE:
            default:
                break;
        }
        return null;
    }

    public void sleep() {
        try {
            Thread.sleep( 1000 );
        } catch ( InterruptedException ex ) {
        }
    }

    public ManagerThreadGloryHelper getHelper() {
        return helper;
    }

    public void setNextState( MState mState ) {
        this.state = mState;
    }
}
