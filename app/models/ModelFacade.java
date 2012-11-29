/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controllers.Secure;
import devices.DeviceFactory;
import devices.IoBoard;
import devices.glory.manager.ManagerInterface;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import models.actions.UserAction;
import models.db.LgBag;
import models.db.LgDeposit;
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
    final static private ManagerInterface manager;
    final static private IoBoard ioBoard;
    private static ModelError modelError = new ModelError();
    private static UserAction currentUserAction = null;
    private static User currentUser = null;

    static {
        manager = DeviceFactory.getGloryManager();
        manager.addObserver(new Observer() {
            public void update(Observable o, Object data) {
                Promise now = new OnGloryEvent((ManagerInterface.Status) data).now();
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

        ManagerInterface.Status status;

        public OnGloryEvent(ManagerInterface.Status status) {
            this.status = status;
        }

        @Override
        public void doJob() throws Exception {
            GloryEvent.save(currentUserAction, status.name());
            switch (status.getState()) {
                //Could happen on startup
                case INITIALIZING:
                case IDLE:
                    if (currentUserAction != null) {
                        currentUserAction.onGloryEvent(status);
                    }
                    break;
                case ERROR:
                    modelError.gloryError = status.getErrorDetail();
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
            if (status == null) {
                Logger.debug("doJob status is null");
                return;
            }
            IoBoardEvent.save(currentUserAction, status.toString());
            if (status.isError()) {
                // A development option
                if (!Configuration.ioBoardIgnore()) {
                    modelError.ioBoardError = status.getError();
                }
                return;
            }

            // Bag change.
            if (status.bagState == IoBoard.BAG_STATE.BAG_STATE_INPLACE) {
                switch (status.bagAproveState) {
                    case BAG_NOT_APROVED:
                        ioBoard.aproveBag();
                        break;
                    case BAG_APROVE_WAIT:
                    case BAG_APROVED:
                        break;
                    case BAG_APROVE_CONFIRM:
                        LgBag.rotateBag(true);
                        ioBoard.aproveBagConfirm();
                        break;
                }
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
                    setError(String.format("count can't start glory %s", manager.getStatus().getErrorDetail()));
                }
            }
        }

        public boolean store(Integer depositId) {
            return manager.storeDeposit(depositId);
        }

        public void withdraw() {
            if (!manager.withdrawDeposit()) {
                setError("withdraw can't withdrawDeposit glory");
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

        public ManagerInterface.State getManagerState() {
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

        public void openGate() {
            ioBoard.openGate();
        }

        public void closeGate() {
            ioBoard.closeGate();
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
        ActionEvent.save(userAction, "Start try", getNeededController());
        if (modelError.isError()) {
            Logger.info("Can't start an action when on error");
            return;
        }
        if (currentUserAction != null || currentUser != null) {
            Logger.error("startAction currentAction is not null");
            return;
        }
        if (!Configuration.ioBoardIgnore()) {
            if (ioBoard.getStatusCopy().bagState != IoBoard.BAG_STATE.BAG_STATE_INPLACE) {
                setError("bag not in place");
                return;
            }
        }

        currentUser = Secure.getCurrentUser();
        currentUserAction = userAction;
        ActionEvent.save(currentUserAction, "Start", getNeededController());
        currentUserAction.start(currentUser, new UserActionApi());
    }

    synchronized public static void finishAction() {
        if (currentUserAction != null && currentUser != null) {
            ActionEvent.save(currentUserAction, "Finish", getNeededController());

            if (currentUserAction.canFinishAction() || modelError.isError()) {
                currentUserAction.finish();
                currentUserAction = null;
                currentUser = null;
            }
        }
    }

    synchronized static public String getState() {
        if (isLocked()) {
            return null;
        }
        if (manager.getStatus().getState() == ManagerInterface.State.ERROR) {
            if (Play.configuration.getProperty("glory.ignore") == null) {
                setError(String.format("Glory error : %s", manager.getStatus().getErrorDetail()));
            }
        }

        if (ioBoard.isError()) {
            if (!Configuration.ioBoardIgnore()) {
                setError(String.format("IoBoard error : %s", ioBoard.getError()));
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
            return "CounterController";
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
        ManagerInterface.State m = manager.getStatus().getState();
        if (m != ManagerInterface.State.ERROR) {
            modelError.gloryError = null;
        } else {
            Logger.error("Manager still in error %s", m.name());
        }
        if (ioBoard.isError()) {
            modelError.ioBoardError = null;
        } else {
            Logger.error("IoBoard still in error %s", ioBoard.getError());
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

    public static LgDeposit getDeposit() {
        if (currentUserAction == null) {
            Logger.error("getDeposit invalid current User Action");
            return null;
        }
        if (currentUserAction.getDepositId() == null) {
            Logger.error("getDeposit invalid depositId %d", currentUserAction.getDepositId());
            return null;
        }
        return LgDeposit.findById(currentUserAction.getDepositId());
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
