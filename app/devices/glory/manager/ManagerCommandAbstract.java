/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.Glory;
import devices.glory.command.CommandWithDataResponse;
import devices.glory.command.CommandWithDataResponse.D1Mode;
import devices.glory.command.GloryCommandAbstract;
import java.io.IOException;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class ManagerCommandAbstract {

    private final Glory device;

    public ManagerCommandAbstract( Glory device ) {
        this.device = device;
    }

    private CommandWithDataResponse sendGloryCommand( GloryCommandAbstract cmd ) throws IOException {
        CommandWithDataResponse ret = ( CommandWithDataResponse ) device.sendCommand( cmd );
        if ( ret.getError() != null ) {
            Logger.error( "PollThread on start : " + ret.getError() );
            throw new IOException( ret.getError() );
        }
        return ret;
    }

    /**
     * *
     *
     * @throws IOException
     */
    private void gotoNeutral() throws IOException {
        CommandWithDataResponse ret = sendGloryCommand( new devices.glory.command.Sense() );
        switch ( ret.getD1() ) {
            case initial:
                sendGloryCommand( new devices.glory.command.RemoteCancel() );
                ret = sendGloryCommand( new devices.glory.command.Sense() );
                if ( ret.getD1() != D1Mode.neutral ) {
                    throw new IOException( "Not in D1 Mode" );
                }
                break;
            case neutral:
                break;
            default:
                throw new IOException( String.format( "Invalid SR1 mode %d", ret.getSr1() ) );
        }
    }

    abstract boolean execute();

    boolean mustStop() {
        return false;
    }
}
