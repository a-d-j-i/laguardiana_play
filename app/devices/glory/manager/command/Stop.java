/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.manager.GloryManager;
import play.Logger;

/**
 *
 * @author adji
 */
public class Stop extends ManagerCommandAbstract {

    public Stop(GloryManager.ThreadCommandApi threadCommandApi) {
        super(threadCommandApi, null);
    }

    @Override
    public void execute() {
        Logger.debug("EXECUTING STOP");
        gotoNeutral(false, false);
        Logger.debug("EXECUTING STOP DONE");
    }
}
