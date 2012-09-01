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
public class Reset extends ManagerCommandAbstract {

    public Reset(Manager.ThreadCommandApi threadCommandApi, Runnable onCommandDone) {
        super(threadCommandApi, onCommandDone);
    }

    @Override
    public void execute() {
        gotoNeutral(true, false);
        threadCommandApi.setStatus(Manager.Status.IDLE);
    }
}
