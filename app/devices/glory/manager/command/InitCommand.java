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
public class InitCommand extends ManagerCommandAbstract {

    public InitCommand(ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
    }

    @Override
    public void execute() {
        setState(GloryManager.State.INITIALIZING);
        gotoNeutral(false, false, false);
        setState(GloryManager.State.IDLE);
    }
}
