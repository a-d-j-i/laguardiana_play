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
    public void execute() {
        Logger.debug("Reset command");
        if (gotoNeutral(false, false, true)) {
            Logger.debug("Reset command done");
            clearError(false);
        } else {
            Logger.debug("Reset command failed");
        }
    }
}
