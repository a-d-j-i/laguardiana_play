/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.manager.GloryManager.ThreadCommandApi;
import devices.glory.manager.ManagerInterface;

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
        setState(ManagerInterface.State.INITIALIZING);
        gotoNeutral(false, false, false);
        setState(ManagerInterface.State.IDLE);
    }
}
