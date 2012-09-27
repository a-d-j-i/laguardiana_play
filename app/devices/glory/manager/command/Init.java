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
public class Init extends ManagerCommandAbstract {

    public Init(ThreadCommandApi threadCommandApi) {
        super(threadCommandApi, null);
    }

    @Override
    public void execute() {
        setStatus(GloryManager.Status.INITIALIZING, false);
        gotoNeutral(false, false);
        setStatus(GloryManager.Status.IDLE, false);
    }
}
