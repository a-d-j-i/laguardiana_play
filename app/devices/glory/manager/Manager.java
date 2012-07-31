package devices.glory.manager;

import devices.glory.Glory;
import devices.glory.command.CommandWithCountingDataResponse.Bill;
import devices.glory.manager.command.*;
import java.util.ArrayList;
import play.Logger;

/**
 *
 * @author adji
 */
public class Manager {

    private ManagerThreadExecutor managerThreadExecutor = null;
    private ManagerStatus status = null;

    public Manager( Glory device ) {
        status = new ManagerStatus();
        managerThreadExecutor = new ManagerThreadExecutor( status, device );
        managerThreadExecutor.start();
    }

    public boolean billDeposit() {
        int[] bills = new int[ 32 ];
        for ( int i = 0; i < bills.length; i++ ) {
            bills[ i] = 0;
        }
        return managerThreadExecutor.sendCommand( new StartCounting( bills ) );
    }

    public ArrayList<Bill> getBillDepositData() {
        return status.getBillData();
    }

    public boolean cancelDeposit() {
        Logger.debug( "cancelDeposit" );
        managerThreadExecutor.cancelLastCommand();
        return managerThreadExecutor.sendCommand( new CancelDeposit() );
    }

    public boolean storeDeposit( int sequenceNumber ) {
        Logger.debug( "storeDeposit" );
        return managerThreadExecutor.sendCommand( new StoreDeposit( sequenceNumber ) );
    }

    public boolean reset() {
        Logger.debug( "------reset" );
        return managerThreadExecutor.sendCommand( new Reset() );
    }

    public void startCounting() {
        Logger.debug( "startCounting" );

    }

    public void configure() {
        Logger.debug( "configure" );
    }

    public void close() {
        managerThreadExecutor.cancelLastCommand();
        managerThreadExecutor.sendCommand( new Stop() );
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
