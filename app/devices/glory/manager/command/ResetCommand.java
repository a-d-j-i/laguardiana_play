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
public class ResetCommand extends ManagerCommandAbstract {

    public ResetCommand(GloryManager.ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
    }

    @Override
    public void run() {
        Logger.debug("Reset command");
        clearError();
        if (gotoNeutral(false, true)) {
            Logger.debug("Reset command done");
        } else {
            Logger.debug("Reset command failed");
        }
    }
}
