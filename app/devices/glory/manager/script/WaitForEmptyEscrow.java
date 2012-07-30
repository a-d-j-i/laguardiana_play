/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.script;

import devices.glory.manager.ManagerThread;
import play.Logger;

/**
 *
 * @author adji
 */
public class WaitForEmptyEscrow {

    static public String execute( ManagerThread thread ) {
        if ( !thread.getHelper().sense() ) {
            return thread.getHelper().getError();
        }
        Logger.debug( String.format( "D1 Mode %s SR1 Mode : %s", thread.getHelper().getStatus().getD1Mode().name(), thread.getHelper().getStatus().getSr1Mode().name() ) );
        switch ( thread.getHelper().getStatus().getSr1Mode() ) {
            case being_restoration:
                thread.sleep();
                break;
            case escrow_close_request:
                if ( !thread.getHelper().sendGloryCommand( new devices.glory.command.CloseEscrow() ) ) {
                    return thread.getHelper().getError();
                }
                break;
            case escrow_open:
            case escrow_close:
                thread.sleep();
                break;
            case waiting:
                thread.setNextState( ManagerThread.MState.WAIT );
                break;
            default:
                return String.format( "invalid sr1 mode", thread.getHelper().getStatus().getSr1Mode().name() );
        }
        return null;
    }
}
