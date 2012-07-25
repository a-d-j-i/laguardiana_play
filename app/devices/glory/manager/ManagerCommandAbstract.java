/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.Glory;
import devices.glory.GloryStatus.D1Mode;
import devices.glory.command.CommandWithDataResponse;
import devices.glory.command.GloryCommandAbstract;
import java.io.IOException;

/**
 *
 * @author adji
 */
abstract public class ManagerCommandAbstract {

    private final ManagerStatus status;
    private boolean cancel = false;

    public ManagerCommandAbstract( ManagerStatus status ) {
        this.status = status;
    }

    public void cancel() {
        cancel = true;
    }

    abstract void execute();

    boolean mustStop() {
        return false;
    }

    protected Glory getDevice() {
        return status.getDevice();
    }

    protected boolean sendGloryCommand( GloryCommandAbstract cmd ) {
        CommandWithDataResponse ret = ( CommandWithDataResponse ) getDevice().sendCommand( cmd );
        if ( ret.getError() != null ) {
            status.addError( "sendGloryCommand : " + ret.getError() );
            return false;
        }
        Sense();
        return true;
    }

    protected void Sense() {
        CommandWithDataResponse cmd = new devices.glory.command.Sense();
        getDevice().sendCommand( cmd );
        status.setStatus( cmd );
    }

    /**
     * *
     *
     * @throws IOException
     */
    protected boolean gotoNeutral() {
        Sense();
        switch ( status.getD1Mode() ) {
            case initial:
                sendGloryCommand( new devices.glory.command.RemoteCancel() );
                if ( status.getD1Mode() != D1Mode.neutral ) {
                    status.addError( "cant set neutral mode" );
                    return false;
                }
                break;
            case neutral:
                break;
            default:
                status.addError( String.format( "Invalid D11 mode %s", status.getD1Mode().name() ) );
                return false;
        }
        return true;
    }

    protected boolean isError() {
        return status.isError();
    }
}
