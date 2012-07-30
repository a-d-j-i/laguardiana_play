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
public class ProcessCount {

    static public String execute( ManagerThread thread ) {
        int[] bills = new int[ 32 ];
        for ( int i = 0; i < bills.length; i++ ) {
            bills[ i] = 0;
        }
        if ( !thread.getHelper().sense() ) {
            return thread.getHelper().getError();
        }
        Logger.debug( String.format( "SR1 Mode : %s", thread.getHelper().getStatus().getSr1Mode().name() ) );
        switch ( thread.getHelper().getStatus().getSr1Mode() ) {
            case storing_start_request:
                if ( thread.getHelper().getStatus().isHopperBillPresent() ) {
                    if ( !thread.getHelper().sendGloryCommand( new devices.glory.command.BatchDataTransmition( bills ) ) ) {
                        return thread.getHelper().getError();
                    }
                    break;
                }
            case counting:
            case waiting:
                thread.sleep();
                break;
            case counting_start_request:
                if ( !thread.getHelper().sendGloryCommand( new devices.glory.command.BatchDataTransmition( bills ) ) ) {
                    return thread.getHelper().getError();
                }
                break;
            default:
                return String.format( "invalid sr1 mode", thread.getHelper().getStatus().getSr1Mode().name() );
        }
        return null;
    }
}
