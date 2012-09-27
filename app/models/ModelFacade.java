/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controllers.Secure;
import devices.CounterFactory;
import devices.glory.manager.GloryManager;
import devices.glory.manager.GloryManager.ErrorDetail;
import devices.io_board.IoBoard;
import java.util.Observable;
import java.util.Observer;
import models.actions.UserAction;
import models.db.LgEvent;
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
    final static private GloryManager.ControllerApi manager = CounterFactory.getGloryManager();
    final static private IoBoard ioBoard = CounterFactory.getIoBoard();

    static class GloryActionCalblack extends Job {

        GloryManager.Status status;
        ErrorDetail errorDetail;

        public GloryActionCalblack(GloryManager.Status status, ErrorDetail errorDetail) {
            this.status = status;
            this.errorDetail = errorDetail;
        }

        @Override
        public void doJob() throws Exception {
            LgEvent.save(currentUserAction.getDeposit(), LgEvent.Type.GLORY, status.name());
            currentUserAction.gloryDone(status, errorDetail);
        }
    }

    static class IoBoardActionCalblack extends Job {

        IoBoard.IoBoardStatus status;

        public IoBoardActionCalblack(IoBoard.IoBoardStatus status) {
            this.status = status;
        }

        @Override
        public void doJob() throws Exception {
            LgEvent.save(currentUserAction.getDeposit(), LgEvent.Type.IO_BOARD, status.toString());
            //TODO: Call the cops.
            currentUserAction.ioBoardEvent(status);
        }
    }

    static {
        ioBoard.addObserver(new WhenIoBoardDone());
    }

    static protected class WhenIoBoardDone implements Observer {

        public void update(Observable o, Object status) {
            if (currentUserAction == null) {
                Logger.error("Current user action is null");
            } else {
                Promise now = new IoBoardActionCalblack((IoBoard.IoBoardStatus) status).now();
            }
        }
    }

    static protected class WhenGloryDone implements Runnable {

        public void run() {
            // TODO: Pass this to the orchestrator as an event.
            if (currentUserAction == null) {
                Logger.error("Current user action is null");
            } else {
                Promise now = new GloryActionCalblack(manager.getStatus(), manager.getErrorDetail()).now();
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

        public GloryManager.Status getManagerStatus() {
            return manager.getStatus();
        }

        public boolean storeDeposit(Integer depositId) {
            return manager.storeDeposit(depositId);
        }

        public boolean withdrawDeposit() {
            return manager.withdrawDeposit();
        }

        public boolean cancelDeposit() {
            return manager.cancelDeposit(whenGloryDone);
        }

        public GloryManager.ErrorDetail getErrorDetail() {
            return manager.getErrorDetail();
        }

        public void finishAction() {
            ModelFacade.finishAction();
        }

        public boolean envelopeDeposit() {
            synchronized (ModelFacade.class) {
                if (ModelFacade.currentUserAction == null) {
                    Logger.error("count currentAction is null");
                    return false;
                }
                return manager.envelopeDeposit(whenGloryDone);
            }
        }
    }

    synchronized static public void startAction(UserAction userAction) {
        LgEvent.save(userAction.getDeposit(), LgEvent.Type.ACTION_START_TRY, userAction.getNeededController());
        if (currentUserAction != null || currentUser != null) {
            Logger.error("startAction currentAction is not null");
            return;
        }
        currentUser = Secure.getCurrentUser();
        currentUserAction = userAction;
        LgEvent.save(currentUserAction.getDeposit(), LgEvent.Type.ACTION_START, userAction.getNeededController());
        currentUserAction.start(currentUser, new UserActionApi());
    }

    synchronized private static void finishAction() {
        if (currentUserAction == null || currentUser == null) {
            Logger.error("finishDeposit currentAction is null");
        }
        LgEvent.save(currentUserAction.getDeposit(), LgEvent.Type.ACTION_FINISH, currentUserAction.getNeededController());
        currentUserAction = null;
        currentUser = null;
    }
}
