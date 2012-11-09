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
public class CollectCommand extends ManagerCommandAbstract {

    public CollectCommand(ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
    }

    @Override
    public void execute() {
        setState(GloryManager.State.COLLECTING);
        if (sendGloryCommand(new devices.glory.command.SetCollectMode())) {
            gotoNeutral(false, false, false);
        }
    }
}
