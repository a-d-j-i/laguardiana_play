/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.manager.Manager;
import devices.glory.manager.Manager.Status;
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
        setStatus(Manager.Status.CANCELING, false);
        gotoNeutral(false, false);
        cancel();
        setStatus(Manager.Status.CANCELED, false);
    }
}
