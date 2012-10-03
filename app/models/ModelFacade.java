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
 *
 * @author aweil
 */
public class ModelFacade {
    // FACTORY

    final static private GloryManager.ControllerApi manager = DeviceFactory.getGloryManager();
    final static private IoBoard ioBoard;
    private static String error = null;
    private static UserAction currentUserAction = null;
    private static User currentUser = null;
    final static private Runnable onGloryDone = new Runnable() {
        public void run() {
            Promise now = new OnGloryEvent(manager.getStatus(), manager.getErrorDetail()).now();
        }
    };

    static {
        ioBoard = DeviceFactory.getIoBoard();
        ioBoard.addObserver(new Observer() {
            public void update(Observable o, Object data) {
                Promise now = new OnIoBoardEvent((IoBoard.IoBoardStatus) data).now();
            }
        });
    }

    static class OnGloryEvent extends Job {

        GloryManager.Status status;
        GloryManager.ErrorDetail me;

        public OnGloryEvent(GloryManager.Status status, GloryManager.ErrorDetail me) {
            this.status = status;
            this.me = me;
        }

        @Override
        public void doJob() throws Exception {
            if (status == GloryManager.Status.ERROR) {
                String msg = String.format("Glory Error : %s", me);
                Event.save(null, Event.Type.GLORY, msg);
                if (Play.configuration.getProperty("glory.ignore") == null) {
                    setError(msg);
                }
                return;
            }

            if (currentUserAction == null) {
                String msg = String.format("Glory current user action is null : %s", status.name());
                Logger.error(msg);
                Event.save(null, Event.Type.GLORY, msg);
            } else {
                Event.save(currentUserAction.getDepositId(), Event.Type.GLORY, status.name());
                currentUserAction.onGloryEvent(status);
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
            if (status.status == IoBoard.Status.ERROR) {
                String msg = String.format("IOBoard Error : %s", status.error);
                Event.save(null, Event.Type.IO_BOARD, msg);
                // A development option
                if (Play.configuration.getProperty("io_board.ignore") == null) {
                    setError(msg);
                }
                return;
            }

            if (currentUserAction == null) {
                Event.save(null, Event.Type.IO_BOARD, status.toString());
            } else {
                //TODO: Call the cops.
                Event.save(currentUserAction.getDepositId(), Event.Type.IO_BOARD, status.toString());
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
                if (!manager.count(onGloryDone, null, numericId)) {
                    setError(String.format("startBillDeposit can't start glory %s", manager.getErrorDetail()));
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
            return manager.cancelDeposit(onGloryDone);
        }

        public void envelopeDeposit() {
            synchronized (ModelFacade.class) {
                if (ModelFacade.currentUserAction == null) {
                    setError("envelopeDeposit currentAction is null");
                    return;
                }
                if (!manager.envelopeDeposit(onGloryDone)) {
                    setError(String.format("envelopeDeposit can't start glory %s", manager.getErrorDetail()));
                }
            }
        }

        public GloryManager.Status getManagerStatus() {
            return manager.getStatus();
        }
    }

    synchronized static public void startAction(UserAction userAction) {
        Event.save(userAction.getDepositId(), Event.Type.ACTION_START_TRY, getNeededController());
        if (currentUserAction != null || currentUser != null) {
            Logger.error("startAction currentAction is not null");
            return;
        }
        currentUser = Secure.getCurrentUser();
        currentUserAction = userAction;
        Event.save(currentUserAction.getDepositId(), Event.Type.ACTION_START, getNeededController());
        currentUserAction.start(currentUser, new UserActionApi());
    }

    synchronized public static void finishAction() {
        if (currentUserAction == null || currentUser == null) {
            Logger.error("finishDeposit currentAction is null");
        } else {
            Event.save(currentUserAction.getDepositId(), Event.Type.ACTION_FINISH, getNeededController());
            currentUserAction.finishAction();
        }
        currentUserAction = null;
        currentUser = null;
    }

    synchronized static public String getState() {
        if (isLocked()) {
            return null;
        }
        if (manager.getStatus() == GloryManager.Status.ERROR) {
            if (Play.configuration.getProperty("glory.ignore") == null) {
                setError(String.format("Glory error : %s", manager.getErrorDetail()));
            }
        }
        if (ioBoard.getStatus().status == IoBoard.Status.ERROR) {
            if (Play.configuration.getProperty("io_board.ignore") == null) {
                setError(String.format("IoBoeard error : %s", ioBoard.getStatus().error));
            }
        }
        if (isError()) {
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
        if (isError()) {
            return "Application";
        }
        if (currentUserAction != null) {
            return currentUserAction.getActionNeededController();
        }
        return null;
    }

    synchronized static public String getNeededAction() {
        if (isLocked()) {
            return null;
        }
        if (isError()) {
            return "counterError";
        }
        if (currentUserAction != null) {
            return currentUserAction.getNeededActionAction();
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
        error = e;
        finishAction();
    }

    private static void clearError() {
        GloryManager.Status m = manager.getStatus();
        if (m == GloryManager.Status.ERROR) {
            Logger.error("Manager still in error %s", m.name());
            return;
        }
        IoBoard.IoBoardStatus s = ioBoard.getStatus();
        if (s.status == IoBoard.Status.ERROR) {
            Logger.error("IoBoard still in error %s", m.name());
            return;
        }
        error = null;
    }

    synchronized public static boolean isError() {
        return error != null;
    }

    synchronized public static String getError() {
        return error;
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
        return currentUserAction.getActionMessage();
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

    public static void cancelTimeout() {
        if (currentUserAction == null) {
            Logger.error("cancelTimeout invalid current User Action");
            return;
        }
        currentUserAction.cancelTimer();
    }

    // TODO: Implement this as an action with some screens.
    synchronized public static void reset() {
        if (!isError()) {
            return;
        }
        finishAction();
        manager.reset(new Runnable() {
            public void run() {
                if (manager.getStatus() != GloryManager.Status.ERROR) {
                    clearError();
                }
            }
        });
        ioBoard.clearError();
    }

    // TODO: Implement this as an action with some screens.
    synchronized public static void storingErrorReset() {
        if (!isError()) {
            return;
        }
        finishAction();
        manager.storingErrorReset(new Runnable() {
            public void run() {
                if (manager.getStatus() != GloryManager.Status.ERROR) {
                    clearError();
                }
            }
        });
        ioBoard.clearError();
    }
}
