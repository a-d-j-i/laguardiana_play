/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.manager.Manager;
import play.Logger;

/**
 *
 * @author adji
 */
public class Stop extends ManagerCommandAbstract {

    public Stop( Manager.ThreadCommandApi threadCommandApi ) {
        super( threadCommandApi );
    }

    @Override
    public void execute() {
        Logger.debug( "EXECUTING STOP" );
        gotoNeutral( true, false );
        Logger.debug( "EXECUTING STOP DONE" );
    }
}
