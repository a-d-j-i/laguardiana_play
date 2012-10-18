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
public class StopCommand extends ManagerCommandAbstract {

    public StopCommand(GloryManager.ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
    }

    @Override
    public void execute() {
        Logger.debug("EXECUTING STOP");
        gotoNeutral(false, false, false);
        Logger.debug("EXECUTING STOP DONE");
    }
}
