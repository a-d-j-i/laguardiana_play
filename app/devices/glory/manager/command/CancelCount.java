/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.manager.GloryManager;
import devices.glory.manager.GloryManager.ThreadCommandApi;

/**
 *
 * @author adji
 */
public class CancelCount extends ManagerCommandAbstract {

    public CancelCount(ThreadCommandApi threadCommandApi, Runnable onCommandDone) {
        super(threadCommandApi, onCommandDone);
    }

    @Override
    public void execute() {
        setStatus(GloryManager.Status.CANCELING, false);
        gotoNeutral(false, false);
        cancel();
    }
}
