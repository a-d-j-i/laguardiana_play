/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.manager.Manager.ThreadCommandApi;
import devices.glory.manager.command.Init;
import devices.glory.manager.command.ManagerCommandAbstract;
import devices.glory.manager.command.Stop;
import play.Logger;

/**
 *
 * @author adji
 */
public class ManagerThread extends Thread {

    private final ThreadCommandApi threadCommandApi;

    ManagerThread( Manager.ThreadCommandApi threadCommandApi ) {
        this.threadCommandApi = threadCommandApi;
    }

    @Override
    public void run() {
        ManagerCommandAbstract currentCommand;
        currentCommand = new Init( threadCommandApi );
        currentCommand.run();
        while ( !threadCommandApi.mustStop() ) {
            currentCommand = threadCommandApi.getCurrentCommand();
            if ( currentCommand == null ) {
                Logger.debug( "Manager state null???" );
                continue;
            }
            Logger.debug( String.format( "Manager state : %s", currentCommand.toString() ) );
            currentCommand.run();
        }
        Logger.debug( "Executing stop command" );
        currentCommand = new Stop( threadCommandApi );
        currentCommand.run();
        Logger.debug( "Executin stop command done" );
        threadCommandApi.stopped();
        Logger.debug( "thread stopped" );
    }
}
