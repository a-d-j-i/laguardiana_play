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
public class StoringErrorResetCommand extends ManagerCommandAbstract {

    public StoringErrorResetCommand(GloryManager.ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
    }

    @Override
    public void execute() {
        Logger.debug("StoringErrorReset command");
        if (gotoNeutral(true, true)) {
            Logger.debug("StoringErrorReset command done");
            clearError(false);
        } else {
            Logger.debug("StoringErrorReset command faileds");
        }
    }
}
