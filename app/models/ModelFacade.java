/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import devices.CounterFactory;
import devices.glory.manager.Manager;
import models.actions.UserAction;
import models.actions.UserAction.CurrentStep;
import play.Logger;

/**
 * TODO: Review this with another thread/job that has a input queue for events
 * and react according to events from the glory and the electronics in the cage.
 *
 * @author aweil
 */
public class ModelFacade {
    // FACTORY

    private static UserAction currentUserAction = null;
    final static private WhenGloryDone whenGloryDone = new WhenGloryDone();
    final static private Manager.ControllerApi manager = CounterFactory.getGloryManager();

    static protected class WhenGloryDone implements Runnable {

        public void run() {
            // TODO: Pass this to the orchestrator as an event.
            if (currentUserAction == null) {
                Logger.error("Current user action is null");
            } else {
                currentUserAction.gloryDone(manager.getStatus(), manager.getErrorDetail());
            }
        }
    }

    synchronized static public UserAction getCurrentUserAction() {
        return ModelFacade.currentUserAction;
    }

    static public class UserActionApi {

        public boolean count(Integer numericId) {
            synchronized (ModelFacade.class) {
                if (ModelFacade.currentUserAction == null) {
                    Logger.error("count currentAction is null");
                    return false;
                }
                return manager.count(whenGloryDone, null, numericId);
            }
        }

        public Manager.Status getManagerStatus() {
            return manager.getStatus();
        }

        public boolean storeDeposit(Integer depositId) {
            return manager.storeDeposit(depositId);
        }

        public boolean cancelDeposit() {
            return manager.cancelDeposit(whenGloryDone);
        }
    }

    synchronized static public void startAction(UserAction userAction) {
        if (ModelFacade.currentUserAction != null) {
            Logger.error("startAction currentAction is not null");
            return;
        }
        ModelFacade.currentUserAction = userAction;
        ModelFacade.currentUserAction.start(new UserActionApi());
    }

    synchronized public static void finishDeposit() {
        if (ModelFacade.currentUserAction == null) {
            Logger.error("finishDeposit currentAction is null");
            return;
        }
        CurrentStep c = ModelFacade.currentUserAction.getCurrentStep();
        if (c != CurrentStep.FINISH) {
            Logger.error("finishDeposit Invalid step %s", c.name());
            return;
        }
        ModelFacade.currentUserAction = null;
    }
}
