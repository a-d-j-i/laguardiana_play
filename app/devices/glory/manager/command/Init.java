/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.manager.Manager.ThreadCommandApi;

/**
 *
 * @author adji
 */
public class Init extends ManagerCommandAbstract {

    public Init( ThreadCommandApi threadCommandApi ) {
        super( threadCommandApi );
    }

    @Override
    public void execute() {
        String initError = "Initializing";
        threadCommandApi.setError( initError );
        gotoNeutral( false, false );
        threadCommandApi.compareAndSetError( initError, null );
    }
}
