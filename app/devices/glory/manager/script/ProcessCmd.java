/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.script;

import devices.glory.GloryStatus;
import devices.glory.manager.Manager.ManagerCommand;
import devices.glory.manager.ManagerThread;
import play.Logger;

/**
 *
 * @author adji
 */
public class ProcessCmd {

    static public String execute( ManagerThread thread, ManagerCommand cmd ) {
        if ( cmd == null ) {
            return "Invalid cmd null";
        }
        Logger.debug( "Get Next Command %s", cmd.name() );

        switch ( cmd ) {
            case START_DEPOSIT:
                if ( !thread.getHelper().sendGloryCommand( new devices.glory.command.SetDepositMode() ) ) {
                    return thread.getHelper().getError();
                }
                if ( !thread.getHelper().sense() ) {
                    return thread.getHelper().getError();
                }
                if ( thread.getHelper().getStatus().getD1Mode() != GloryStatus.D1Mode.deposit ) {
                    return String.format( "cant set deposit mode d1 (%s) mode not neutral", thread.getHelper().getStatus().getD1Mode().name() );
                }
                thread.setNextState( ManagerThread.MState.COUNT );
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
}
