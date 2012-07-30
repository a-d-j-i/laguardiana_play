/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.Glory;
import devices.glory.GloryStatus;
import devices.glory.command.GloryCommandAbstract;

/**
 *
 * @author adji
 */
public class ManagerThreadGloryHelper {

    private GloryStatus status;
    private Glory device;

    public ManagerThreadGloryHelper( Glory device ) {
        this.status = new GloryStatus();
        this.device = device;
    }

    public boolean sendGloryCommand( GloryCommandAbstract cmd ) {
        if ( cmd != null ) {
            if ( !status.setStatusOk( device.sendCommand( cmd, true ) ) ) {
                return false;
            }
        }
        return sense();
    }

    public boolean sense() {
        GloryCommandAbstract cmd = new devices.glory.command.Sense();
        device.sendCommand( cmd, true );
        return status.setStatusOk( cmd );
    }

    public boolean isError() {
        return status.isError();
    }

    public String getError() {
        return status.getLastError();
    }

    public GloryStatus getStatus() {
        return status;
    }
}
