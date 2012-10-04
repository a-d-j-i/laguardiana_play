/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controllers.Secure;
import devices.DeviceFactory;
import devices.IoBoard;
import devices.glory.manager.GloryManager;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import models.actions.UserAction;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.libs.F.Promise;

/**
 * TODO: Review this with another thread/job that has a input queue for events
 * and react according to events from the glory and the electronics in the cage.
 * TODO: Save the state on the db so we react better on restart !!!!.
 *
 * @author aweil
 */
public class ModelFacade {

    // FACTORY
    static public class ModelError {

        public String gloryError = null;
        public String ioBoardError = null;
        public String appError = null;

        public boolean isError() {
            return (gloryError != null || ioBoardError != null || appError != null);
        }

        @Override
        public String toString() {
            return "gloryError=" + gloryError + ", ioBoardError=" + ioBoardError + ", appError=" + appError;
        }
    }
    final static private GloryManager.ControllerApi manager;
    final static private IoBoard ioBoard;
    private static ModelError modelError = new ModelError();
    private static UserAction currentUserAction = null;
    private static User currentUser = null;

    static {
        manager = DeviceFactory.getGloryManager();
        manager.addObserver(new Observer() {
            public void update(Observable o, Object data) {
                Promise now = new OnGloryEvent((GloryManager.Status) data).now();
            }
        });

        ioBoard = DeviceFactory.getIoBoard();
        ioBoard.addObserver(new Observer() {
            public void update(Observable o, Object data) {
                Promise now = new OnIoBoardEvent((IoBoard.IoBoardStatus) data).now();
            }
        });
    }

    static class OnGloryEvent extends Job {

        GloryManager.Status status;

        public OnGloryEvent(GloryManager.Status status) {
            this.status = status;
        }

        @Override
        public void doJob() throws Exception {
            Event.save(currentUserAction, Event.Type.GLORY, status.name());
            switch (status.getState()) {
                case ERROR:
                    if (Play.configuration.getProperty("glory.ignore") == null) {
                        modelError.gloryError = status.getErrorDetail();
                    }
                    break;
                //Could happen on startup
                case INITIALIZING:
                case IDLE:
                    if (currentUserAction != null) {
                        currentUserAction.onGloryEvent(status);
                    }
                    break;
                default:
                    if (currentUserAction == null) {
                        Logger.error(String.format("OnGloryEvent current user action is null : %s", status.name()));
                    } else {
                        currentUserAction.onGloryEvent(status);
                    }
                    break;
            }
        }
    }

    static class OnIoBoardEvent extends Job {

        IoBoard.IoBoardStatus status;

        public OnIoBoardEvent(IoBoard.IoBoardStatus status) {
            this.status = status;
        }

        @Override
        public void doJob() throws Exception {
            Event.save(currentUserAction, Event.Type.IO_BOARD, status.toString());
            if (status.status == IoBoard.Status.ERROR) {
                // A development option
                if (Play.configuration.getProperty("io_board.ignore") == null) {
                    modelError.ioBoardError = status.error;
                }
                return;
            }

            if (currentUserAction != null) {
                currentUserAction.onIoBoardEvent(status);
            }
        }
    }

    static public class UserActionApi {

        public void count(Integer numericId) {
            synchronized (ModelFacade.class) {
                if (ModelFacade.currentUserAction == null) {
                    setError("count currentAction is null");
                    return;
                }
                if (!manager.count(null, numericId)) {
                    setError(String.format("startBillDeposit can't start glory %s", manager.getStatus().getErrorDetail()));
                }
            }
        }

        public boolean store(Integer depositId) {
            return manager.storeDeposit(depositId);
        }

        public void withdraw() {
            if (!manager.withdrawDeposit()) {
                setError("startCounting can't withdrawDeposit glory");
            }
        }

        public boolean cancelDeposit() {
            return manager.cancelDeposit();
        }

        public void envelopeDeposit() {
            synchronized (ModelFacade.class) {
                if (ModelFacade.currentUserAction == null) {
                    setError("envelopeDeposit currentAction is null");
                    return;
                }
                if (!manager.envelopeDeposit()) {
                    setError(String.format("envelopeDeposit can't start glory %s", manager.getStatus().getErrorDetail()));
                }
            }
        }

        public GloryManager.State getManagerState() {
            return manager.getStatus().getState();
        }

        public void setError(String msg) {
            ModelFacade.setError(msg);
        }

        public void clearError() {
            // Todo Put int the ResetAction if needed.
            ioBoard.clearError();
            ModelFacade.clearError();
        }
    }

    synchronized public static boolean isError() {
        return modelError.isError();
    }

    synchronized public static void errorReset() {
        manager.reset();
        ioBoard.clearError();
        clearError();
    }

    synchronized public static void storingErrorReset() {
        manager.storingErrorReset();
        ioBoard.clearError();
        clearError();
    }

    synchronized static public void startAction(UserAction userAction) {
        Event.save(userAction, Event.Type.ACTION_START_TRY, getNeededController());
        if (modelError.isError()) {
            Logger.info("Can't start an action when on error");
            return;
        }
        if (currentUserAction != null || currentUser != null) {
            Logger.error("startAction currentAction is not null");
            return;
        }
        currentUser = Secure.getCurrentUser();
        currentUserAction = userAction;
        Event.save(currentUserAction, Event.Type.ACTION_START, getNeededController());
        currentUserAction.start(currentUser, new UserActionApi());
    }

    synchronized public static void finishAction() {
        if (currentUserAction != null && currentUser != null) {
            Event.save(currentUserAction, Event.Type.ACTION_FINISH, getNeededController());
        }
        if (currentUserAction.canFinishAction() || modelError.isError()) {
            currentUserAction = null;
            currentUser = null;
        }
    }

    synchronized static public String getState() {
        if (isLocked()) {
            return null;
        }
        if (manager.getStatus().getState() == GloryManager.State.ERROR) {
            if (Play.configuration.getProperty("glory.ignore") == null) {
                setError(String.format("Glory error : %s", manager.getStatus().getErrorDetail()));
            }
        }

        if (ioBoard.getStatus().status == IoBoard.Status.ERROR) {
            if (Play.configuration.getProperty("io_board.ignore") == null) {
                setError(String.format("IoBoeard error : %s", ioBoard.getStatus().error));
            }
        }
        if (modelError.isError()) {
            return "ERROR";
        }
        if (currentUserAction != null) {
            return currentUserAction.getStateName();
        }

        return "IDLE";
    }

    synchronized static public String getNeededController() {
        if (isLocked()) {
            return null;
        }
        if (modelError.isError()) {
            return "Application";
        }
        if (currentUserAction != null) {
            return currentUserAction.getNeededController();
        }
        return null;
    }

    synchronized static public String getNeededAction() {
        if (isLocked()) {
            return null;
        }
        if (modelError.isError()) {
            return "counterError";
        }
        if (currentUserAction != null) {
            return currentUserAction.getNeededAction();
        }
        return null;
    }

    synchronized public static boolean isLocked() {
        if (currentUser != null && !currentUser.equals(Secure.getCurrentUser())) {
            return true;
        }
        return false;
    }

    private static void setError(String e) {
        Logger.debug("Error in manager %s", e);
        modelError.appError = e;
        finishAction();
    }

    private static void clearError() {
        GloryManager.State m = manager.getStatus().getState();
        if (m != GloryManager.State.ERROR) {
            modelError.gloryError = null;
        } else {
            Logger.error("Manager still in error %s", m.name());
        }
        IoBoard.IoBoardStatus s = ioBoard.getStatus();
        if (s.status != IoBoard.Status.ERROR) {
            modelError.ioBoardError = null;
        } else {
            Logger.error("IoBoard still in error %s", m.name());
        }
        modelError.appError = null;
    }

    synchronized public static String getError() {
        return modelError.toString();
    }

    synchronized public static List<Bill> getCurrentCounters() {
        if (currentUserAction == null) {
            Logger.error("getCurrentCounters invalid current User Action");
            return null;
        }
        if (currentUserAction.getCurrency() == null) {
            Logger.error("getCurrentCounters invalid currency null");
            return null;
        }
        return Bill.getBillList(currentUserAction.getCurrency().numericId);
    }

    synchronized public static Object getFormData() {
        if (currentUserAction == null) {
            Logger.error("getFormData invalid current User Action");
            return null;
        }
        return currentUserAction.getFormData();
    }

    synchronized public static String getActionMessage() {
        if (currentUserAction == null) {
            Logger.error("getActionMessage invalid current User Action");
            return null;
        }
        return currentUserAction.getMessage();
    }

    public static Deposit getDeposit() {
        if (currentUserAction == null) {
            Logger.error("getDeposit invalid current User Action");
            return null;
        }
        if (currentUserAction.getDepositId() == null) {
            Logger.error("getDeposit invalid depositId %d", currentUserAction.getDepositId());
            return null;
        }
        return Deposit.findById(currentUserAction.getDepositId());
    }

    public static Long getDepositTotal() {
        if (currentUserAction == null) {
            Logger.error("getDeposit invalid current User Action");
            return null;
        }
        if (currentUserAction.getDepositId() == null) {
            Logger.error("getDeposit invalid depositId %d", currentUserAction.getDepositId());
            return null;
        }
        return Deposit.getTotal(currentUserAction.getDepositId());
    }

    public static void cancel() {
        if (currentUserAction == null) {
            Logger.error("cancel invalid current User Action");
            return;
        }
        currentUserAction.cancel();
    }

    public static void accept() {
        if (currentUserAction == null) {
            Logger.error("accept invalid current User Action");
            return;
        }
        currentUserAction.accept();
    }

    public static void suspendTimeout() {
        if (currentUserAction == null) {
            Logger.error("cancelTimeout invalid current User Action");
            return;
        }
        currentUserAction.suspendTimeout();
    }
}
