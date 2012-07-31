/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.Glory;
import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.command.Init;
import devices.glory.manager.command.ManagerCommandAbstract;
import play.Logger;

/**
 * TODO: Extend from job, implemente executor....
 *
 * @author adji
 */
public class ManagerThread extends Thread {

    private final Glory device;
    final private ManagerThreadExecutor executor;
    final private ManagerStatus status;

    ManagerThread( ManagerThreadExecutor executor, ManagerStatus status, Glory device ) {
        this.executor = executor;
        this.status = status;
        this.device = device;
    }

    @Override
    public void run() {
        ManagerCommandAbstract currentCommand = new Init();
        Logger.debug( String.format( "Manager state : %s", currentCommand.toString() ) );
        currentCommand.execute( this );
        while ( !mustStop() ) {
            // Blocks until the next command.
            currentCommand = executor.getNextCommand();
            if ( currentCommand == null ) {
                continue;
            }
            Logger.debug( String.format( "Manager state : %s", currentCommand.toString() ) );
            Logger.debug( "Get Next Command %s", currentCommand.toString() );
            currentCommand.execute( this );
        }
    }

    public void sleep() {
        try {
            Thread.sleep( 3000 );
        } catch ( InterruptedException ex ) {
        }
    }

    public boolean sendGCommand( GloryCommandAbstract cmd ) {
        if ( cmd == null ) {
            setError( "Invalid command null" );
            return false;
        }
        if ( !status.setStatusOk( device.sendCommand( cmd ) ) ) {
            String error = status.getGloryError();
            setError( error );
            return false;
        }
        return true;
    }

    public void setError( String error ) {
        Logger.error( String.format( "Manager Thread error : %s", error ) );
        status.setError( error );
    }

    public ManagerStatus getStatus() {
        return status;
    }

    public boolean mustStop() {
        return executor.mustStop();
    }

    public boolean mustCancel() {
        return executor.mustCancel();
    }
}
