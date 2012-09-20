/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.manager.Manager;

/**
 *
 * @author adji
 */
public class StoringErrorReset extends ManagerCommandAbstract {

    public StoringErrorReset(Manager.ThreadCommandApi threadCommandApi, Runnable onCommandDone) {
        super(threadCommandApi, onCommandDone);
    }

    @Override
    public void execute() {
        gotoNeutral(true, true);
        setStatus(Manager.Status.IDLE, false);
    }
}
