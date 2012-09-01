/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.manager.Manager;
import devices.glory.manager.Manager.ThreadCommandApi;

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
        threadCommandApi.setStatus(Manager.Status.INITIALIZING);
        gotoNeutral(false, false);
        threadCommandApi.setStatus(Manager.Status.INITIALIZING, Manager.Status.IDLE);
    }
}
