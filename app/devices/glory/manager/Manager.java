package devices.glory.manager;

import devices.glory.Glory;
import play.Logger;

/**
 *
 * @author adji
 */
public class Manager {

    public enum ManagerCommand {

        START_DEPOSIT,
        STOP, NONE
    }
    private ManagerThreadExecutor managerThreadExecutor = null;
    private ManagerStatus status = null;

    public Manager( Glory device ) {
        status = new ManagerStatus( device );
        managerThreadExecutor = new ManagerThreadExecutor( status );
        Logger.debug( "Start manager thread" );
        managerThreadExecutor.start();
    }

    public boolean startDeposit() {
        Logger.debug( "startDeposit" );
        return managerThreadExecutor.sendCommand( ManagerCommand.START_DEPOSIT );

    }

    public void billDeposit() {
        Logger.debug( "billDeposit" );
    }

    public void storeDeposit() {
        Logger.debug( "storeDeposit" );
    }

    public void startCounting() {
        Logger.debug( "startCounting" );

    }

    public void configure() {
        Logger.debug( "configure" );
    }

    public void close() {
        managerThreadExecutor.cancelLastCommand();
        managerThreadExecutor.sendCommand( ManagerCommand.STOP );
        managerThreadExecutor.stop();
        try {
            managerThreadExecutor.join( 10000 );
        } catch ( InterruptedException ex ) {
        }
    }

    public String getError() {
        return status.getError();
    }
}
