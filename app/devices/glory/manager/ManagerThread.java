/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.manager.GloryManager.ThreadCommandApi;
import devices.glory.manager.command.GotoNeutral;
import devices.glory.manager.command.ManagerCommandAbstract;
import play.Logger;

/**
 *
 * @author adji
 */
public class ManagerThread extends Thread {

    private final ThreadCommandApi threadCommandApi;

    ManagerThread(GloryManager.ThreadCommandApi threadCommandApi) {
        this.threadCommandApi = threadCommandApi;
    }

    @Override
    public void run() {
        ManagerCommandAbstract currentCommand = null;
        ManagerCommandAbstract gotoNeutral = new GotoNeutral(threadCommandApi);

        while (!threadCommandApi.mustStop()) {
            Logger.debug("Manager state : Executing goto neutral");
            gotoNeutral.run();
            Logger.debug("Manager state : Executing goto neutral DONE");

            currentCommand = null;
            while (!threadCommandApi.mustStop() && currentCommand == null) {
                currentCommand = threadCommandApi.getCurrentCommand();
                if (currentCommand == null) {
                    Logger.debug("Manager state null???");
                    continue;
                }
            }
            if (currentCommand != null) {
                Logger.debug(String.format("Manager state : %s", currentCommand.getClass().getSimpleName()));
                currentCommand.run();
            }
            threadCommandApi.currentCommandDone();
        }
        Logger.debug("Executing GotoNeutral command on Stop");
        currentCommand = new GotoNeutral(threadCommandApi);
        currentCommand.run();
        Logger.debug("Executing GotoNeutral command on Stop done");
        threadCommandApi.stopped();
        Logger.debug("thread stopped");
    }
}
