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
public class GotoNeutral extends ManagerCommandAbstract {

    public GotoNeutral(ThreadCommandApi threadCommandApi) {
        super(threadCommandApi);
    }

    @Override
    public void run() {
        setState(ManagerInterface.State.INITIALIZING);
        gotoNeutral(false, false);
    }
}
