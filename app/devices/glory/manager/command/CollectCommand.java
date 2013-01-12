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
public class CollectCommand extends ManagerCommandAbstract {

    public CollectCommand(ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
    }

    @Override
    public void execute() {
        setState(ManagerInterface.State.COLLECTING);
        if (sendGloryCommand(new devices.glory.command.SetCollectMode())) {
            gotoNeutral(false, false);
        }
    }
}
