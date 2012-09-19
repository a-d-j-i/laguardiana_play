/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controllers.Secure;
import devices.CounterFactory;
import devices.glory.manager.Manager;
import devices.glory.manager.Manager.ErrorDetail;
import devices.glory.manager.Manager.Status;
import models.actions.UserAction;
import play.Logger;
import play.jobs.Job;
import play.libs.F;
import play.libs.F.Promise;

/**
 * TODO: Review this with another thread/job that has a input queue for events
 * and react according to events from the glory and the electronics in the cage.
 *
 * @author aweil
 */
public class ModelFacade {
    // FACTORY

    private static UserAction currentUserAction = null;
    private static User currentUser = null;
    final static private WhenGloryDone whenGloryDone = new WhenGloryDone();
    final static private Manager.ControllerApi manager = CounterFactory.getGloryManager();

    static class ActionCallback extends Job {

        Manager.Status status;
        ErrorDetail errorDetail;

        public ActionCallback(Status status, ErrorDetail errorDetail) {
            this.status = status;
            this.errorDetail = errorDetail;
        }

        @Override
        public void doJob() throws Exception {
            currentUserAction.gloryDone(status, errorDetail);
        }
    }

    static protected class WhenGloryDone implements Runnable {

        public void run() {
            // TODO: Pass this to the orchestrator as an event.
            if (currentUserAction == null) {
                Logger.error("Current user action is null");
            } else {
                Promise now = new ActionCallback(manager.getStatus(), manager.getErrorDetail()).now();
            }
        }
    }

    synchronized static public F.Tuple<UserAction, Boolean> getCurrentUserAction() {
        if (currentUser == null) {
            if (currentUserAction != null) {
                Logger.error("getCurrentStep Invalid action state %s", currentUserAction.getActionState());
            }
            return new F.Tuple<UserAction, Boolean>(null, true);
        }
        if (currentUser.equals(Secure.getCurrentUser())) {
            return new F.Tuple<UserAction, Boolean>(currentUserAction, true);
        } else {
            return new F.Tuple<UserAction, Boolean>(null, false);
        }
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

        public Manager.ErrorDetail getErrorDetail() {
            return manager.getErrorDetail();
        }

        public void finishAction() {
            ModelFacade.finishAction();
        }
    }

    synchronized static public void startAction(UserAction userAction) {
        if (currentUserAction != null || currentUser != null) {
            Logger.error("startAction currentAction is not null");
            return;
        }
        currentUser = Secure.getCurrentUser();
        currentUserAction = userAction;
        currentUserAction.start(currentUser, new UserActionApi());
    }

    synchronized private static void finishAction() {
        if (currentUserAction == null || currentUser == null) {
            Logger.error("finishDeposit currentAction is null");
        }
        currentUserAction = null;
        currentUser = null;
    }
}
