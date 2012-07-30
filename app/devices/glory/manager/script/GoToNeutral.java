/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.script;

import devices.glory.GloryStatus;
import devices.glory.manager.ManagerThread;
import play.Logger;

/**
 *
 * @author adji
 */
public class GoToNeutral {

    static public String execute( ManagerThread thread ) {
        if ( !thread.getHelper().sense() ) {
            return String.format( "Error in sense %s", thread.getHelper().getStatus().getLastError() );
        }
        Logger.debug( String.format( "D1Mode : %s SR1Mode %s", thread.getHelper().getStatus().getD1Mode().name(), thread.getHelper().getStatus().getSr1Mode().name() ) );
        switch ( thread.getHelper().getStatus().getD1Mode() ) {
            case deposit:
                switch ( thread.getHelper().getStatus().getSr1Mode() ) {
                    case storing_start_request:
                        thread.setNextState( ManagerThread.MState.CANCEL_EMPTY_ESCROW );
                        return null;
                    case escrow_close_request:
                    case being_restoration:
                        thread.setNextState( ManagerThread.MState.CANCEL_WAIT_FOR_EMPTY_ESCROW );
                        return null;
                    case counting_start_request:
                        return "Remove bills from hoper";
                    default:
                        break;
                }
            case manual:
            case normal_error_recovery_mode:
            case storing_error_recovery_mode:
            case initial:
                if ( !thread.getHelper().sendGloryCommand( new devices.glory.command.RemoteCancel() ) ) {
                    return thread.getHelper().getStatus().getLastError();
                }
                if ( thread.getHelper().getStatus().getD1Mode() != GloryStatus.D1Mode.neutral ) {
                    return String.format( "cant set neutral mode d1 (%s) mode not neutral", thread.getHelper().getStatus().getD1Mode().name() );
                }
                break;
            case neutral:
                break;
            default:
                return String.format( "Invalid D1 mode %s", thread.getHelper().getStatus().getD1Mode().name() );
        }
        thread.setNextState( ManagerThread.MState.WAIT );
        return null;
    }
}
