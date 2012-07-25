/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.Glory;

/**
 *
 * @author adji
 */
public class Manager {

    private Executor executor = null;
    private ManagerStatus status = null;

    public Manager( Glory device ) {
        status = new ManagerStatus( device );
        executor = new Executor();
    }

    public void start() {
        executor.start();
    }

    public boolean startDeposit() {
        return executor.sendCommand( new ManagerCommandDeposit( status ) );
    }

    public boolean cancelDeposit() {
        if ( !executor.cancelLastCommand() ) {
            return false;
        }
        return executor.sendCommand( new ManagerCommandCancelDeposit( status ) );
    }

    public void envelopeDeposit() {
    }

    public void billDeposit() {
    }

    public void storeDeposit() {
    }

    public void startCounting() {
    }

    public void configure() {
    }

    public void close() {
        while ( executor.sendCommand( new ManagerCommandStop( status ) ) ) {
            try {
                executor.join( 1000 );
            } catch ( InterruptedException ex ) {
            }
        }
    }
}
