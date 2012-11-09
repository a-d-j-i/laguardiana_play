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
public class CancelCountCommand extends ManagerCommandAbstract {

    public CancelCountCommand(ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
    }

    @Override
    public void execute() {
        setState(GloryManager.State.CANCELING);
        gotoNeutral(false, false, false);
        cancel();
    }
}
