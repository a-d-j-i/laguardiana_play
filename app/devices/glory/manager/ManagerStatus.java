/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.Glory;
import devices.glory.GloryStatus.D1Mode;
import devices.glory.GloryStatus.SR1Mode;
import devices.glory.command.CommandWithDataResponse;
import play.Logger;

/**
 *
 * @author adji
 */
public class ManagerStatus {

    private Glory device;
    private boolean error = false;
    private D1Mode d1mode = D1Mode.unknown;
    private SR1Mode sr1mode = SR1Mode.unknown;

    public ManagerStatus( Glory device ) {
        this.device = device;
    }

    public Glory getDevice() {
        return device;
    }

    synchronized void addError( String string ) {
        Logger.error( string );
        error = true;
    }

    synchronized boolean isError() {
        return error;
    }

    synchronized D1Mode getD1Mode() {
        return d1mode;
    }

    synchronized SR1Mode getSR1Mode() {
        return sr1mode;
    }

    synchronized void setStatus( CommandWithDataResponse cmd ) {
        if ( cmd.getError() != null ) {
            addError( "sendGloryCommand : " + cmd.getError() );
            return;
        }
        d1mode = cmd.getD1();
        sr1mode = cmd.getSr1();
    }
}
