/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import controllers.GloryController;

/**
 *
 * @author adji
 */
public class Manager {

    private Executor executor = null;

    public Manager( GloryController device ) {
        executor = new Executor();
        executor.start();
    }

    public void startDeposit() {
    }

    public void envelopeDeposit() {
    }

    public void billDeposit() {
    }

    public void storeDeposit() {
    }

    public void cancelDeposit() {
    }

    public void startCounting() {
    }

    public void configure() {
    }

    public void close() {/*
         * while ( executor.sendCommand( ThCommand.STOP ) ) { try {
         * executor.join( 1000 ); } catch ( InterruptedException ex ) { } }
         */

    }
}
