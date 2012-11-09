/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.manager.GloryManager.ThreadCommandApi;
import devices.glory.manager.command.InitCommand;
import devices.glory.manager.command.ManagerCommandAbstract;
import devices.glory.manager.command.StopCommand;
import play.Logger;

/**
 *
 * @author adji
 */
public class ManagerThread extends Thread {

    private final ThreadCommandApi threadCommandApi;

    ManagerThread( GloryManager.ThreadCommandApi threadCommandApi ) {
        this.threadCommandApi = threadCommandApi;
    }

    @Override
    public void run() {
        ManagerCommandAbstract currentCommand;
        currentCommand = new InitCommand( threadCommandApi );
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
        currentCommand = new StopCommand( threadCommandApi );
        currentCommand.run();
        Logger.debug( "Executin stop command done" );
        threadCommandApi.stopped();
        Logger.debug( "thread stopped" );
    }
}
