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
public class CancelCount extends ManagerCommandAbstract {

    public CancelCount(ThreadCommandApi threadCommandApi, Runnable onCommandDone) {
        super(threadCommandApi, onCommandDone);
    }

    @Override
    public void execute() {
        threadCommandApi.setStatus(Manager.Status.CANCELING);
        gotoNeutral(false, false);
        cancel();
        threadCommandApi.setStatus(Manager.Status.CANCELED);
    }
}
