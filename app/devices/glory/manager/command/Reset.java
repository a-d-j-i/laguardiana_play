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
public class Reset extends ManagerCommandAbstract {

    public Reset(GloryManager.ThreadCommandApi threadCommandApi, Runnable onCommandDone) {
        super(threadCommandApi, onCommandDone);
    }

    @Override
    public void execute() {
        Logger.debug("Reset command");
        if (gotoNeutral(true, false)) {
            Logger.debug("Reset command done");
            clearError(false);
        } else {
            Logger.debug("Reset command failed");
        }
    }
}
