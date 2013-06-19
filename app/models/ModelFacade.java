/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controllers.Secure;
import devices.DeviceFactory;
import devices.glory.Glory;
import devices.glory.manager.ManagerInterface;
import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.ioboard.IoBoard;
import devices.printer.Printer;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import models.actions.UserAction;
import models.db.LgBag;
import models.db.LgBillType;
import models.db.LgDeposit;
import models.events.ActionEvent;
import models.events.GloryEvent;
import models.events.IoBoardEvent;
import models.events.PrinterEvent;
import models.lov.Currency;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.libs.F;
import play.libs.F.Promise;

/**
 * TODO: Review this with another thread/job that has a input queue for events
 * and react according to events from the glory and the electronics in the cage.
 * TODO: Save the state on the db so we react better on restart !!!!.
 *
 * @author aweil
 */
public class ModelFacade {

    final static private ManagerInterface manager;
    final static private IoBoard ioBoard;
    final static private ModelError modelError = new ModelError();
    final static private Printer printer;
    static private UserAction currentUserAction = null;
    static private User currentUser = null;

    static {
        manager = DeviceFactory.getGloryManager(Play.configuration.getProperty("glory.port"));
        manager.addObserver(new Observer() {
            public void update(Observable o, Object data) {
                Promise now = new OnGloryEvent((ManagerStatus) data).now();
            }
        });

        ioBoard = DeviceFactory.getIoBoard(Play.configuration.getProperty("io_board.port"));
        ioBoard.addObserver(new Observer() {
            public void update(Observable o, Object data) {
                Promise now = new OnIoBoardEvent((IoBoard.IoBoardStatus) data).now();
            }
        });

        printer = DeviceFactory.getPrinter(Play.configuration.getProperty("printer.port"));
        printer.addObserver(new Observer() {
            public void update(Observable o, Object data) {
                Promise now = new OnPrinterEvent((Printer.PrinterStatus) data).now();
            }
        });
    }

    public static Printer getCurrentPrinter() {
        return printer;
    }

    public static ModelError getError() {
        return modelError;
    }

    static class OnGloryEvent extends Job {

        ManagerStatus status;

        public OnGloryEvent(ManagerStatus status) {
            this.status = status;
        }

        @Override
        public void doJob() throws Exception {
            UserAction u;
            synchronized (ModelFacade.class) {
                u = currentUserAction;
            }
            GloryEvent.save(u, status.toString());
            Logger.debug("OnGloryEvent event %s", status.toString());
            switch (status.getState()) {
                //Could happen on startup
                case INITIALIZING:
                case NEUTRAL:
                    if (u != null) {
                        u.onGloryEvent(status);
                    }
                    break;
                // Dont aprove the bag if not collected
                case BAG_COLLECTED:
                    Logger.debug("-------> BAG COLLECTED, aprove change");
                    //ioBoard.aproveBag();
                    break;
                case ERROR:
                    if (u != null) {
                        u.cancel();
                    }
                    if (status.getError() != null) {
                        modelError.setError(status.getError());
                    }
                    break;
                default:
                    if (u == null) {
                        Logger.error(String.format("OnGloryEvent current user action is null : %s", status.name()));
                    } else {
                        u.onGloryEvent(status);
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
            UserAction u;
            synchronized (ModelFacade.class) {
                u = currentUserAction;
            }
            IoBoardEvent.save(u, status.toString());
            Logger.debug("OnIoBoardEvent event %s", status.toString());

            if (status.getCriticalEvent() != null) {
                IoBoardEvent.save(u, status.getCriticalEvent());
            }

            // Bag change.
            if (status.getBagState() == IoBoard.BAG_STATE.BAG_STATE_INPLACE) {
                switch (status.getBagAproveState()) {
                    case BAG_NOT_APROVED:
                        ioBoard.aproveBag();
                        /*if (!manager.collect()) {
                         modelError.setError(ModelError.ERROR_CODE.ERROR_TRYING_TO_COLLECT, "error trying to collect");
                         }*/
                        break;
                    case BAG_APROVE_WAIT:
                        break;
                    case BAG_APROVED:
                        // Bag aproved, recover from error.
                        if (modelError.getErrorCode() == ModelError.ERROR_CODE.BAG_NOT_INPLACE) {
                            errorReset();
                            //clearError();
                        }
                        break;
                    case BAG_APROVE_CONFIRM:
                        LgBag.rotateBag(true);
                        ioBoard.aproveBagConfirm();
                        break;
                }
                /*                if (u != null
                 && status.getBagAproveState() != IoBoard.BAG_APROVE_STATE.BAG_APROVED
                 && !Configuration.isIgnoreBag()) {
                 modelError.setError(ModelError.ERROR_CODE.BAG_NOT_INPLACE, "Bag rotated during deposit");
                 }*/
            }

            if (u == null) {
                Logger.error(String.format("OnIoBoardEvent current user action is null : %s", status));
            } else {
                u.onIoBoardEvent(status);
            }
        }
    }

    static class OnPrinterEvent extends Job {

        Printer.PrinterStatus status;

        private OnPrinterEvent(Printer.PrinterStatus status) {
            this.status = status;
        }

        @Override
        public void doJob() throws Exception {
            UserAction u;
            synchronized (ModelFacade.class) {
                u = currentUserAction;
            }
            PrinterEvent.save(u, status.toString());
            Logger.debug("OnPrinterEvent event %s", status.toString());
            if (status.getError() != null) {
                if (!Configuration.isPrinterIgnore()) {
                    // A development option
                    Logger.error("Setting printer error : %s", status.toString());
                    modelError.setError(status.getError());
                }
                return;
            }
            if (u != null) {
                u.onPrinterEvent(status);
            }
        }
    }

    static public class UserActionApi {

        public void count(Integer numericId) {
            synchronized (ModelFacade.class) {
                if (ModelFacade.currentUserAction == null) {
                    setError(ModelError.ERROR_CODE.APP_ERROR, "count currentAction is null");
                    return;
                }
                if (!manager.count(null, numericId)) {
                    setError(ModelError.ERROR_CODE.APP_ERROR, String.format("Glory error %s", manager.getStatus()));
                }
            }
        }

        public boolean store(Integer depositId) {
            if (!isIoBoardOk()) {
                return false;
            }
            return manager.storeDeposit(depositId);
        }

        public void withdraw() {
            if (!manager.withdrawDeposit()) {
                setError(ModelError.ERROR_CODE.APP_ERROR, String.format("Glory error %s", manager.getStatus()));
            }
        }

        public void cancelDeposit() {
            manager.cancelCommand();
        }

        public void envelopeDeposit() {
            synchronized (ModelFacade.class) {
                if (ModelFacade.currentUserAction == null) {
                    setError(ModelError.ERROR_CODE.APP_ERROR, "envelopeDeposit currentAction is null");
                    return;
                }
                if (!manager.envelopeDeposit()) {
                    setError(ModelError.ERROR_CODE.APP_ERROR, String.format("Glory error %s", manager.getStatus()));
                }
            }
        }

        public ManagerInterface.MANAGER_STATE getManagerState() {
            return manager.getStatus().getState();
        }

        public void setError(ModelError.ERROR_CODE errorCode, String detail) {
            modelError.setError(errorCode, detail);
            finishAction();
        }

        public void openGate() {
            ioBoard.openGate();
        }

        public void closeGate() {
            ioBoard.closeGate();
        }

        public boolean isIoBoardOk() {
            return ModelFacade.isIoBoardOk(ioBoard.getStatus());
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
        if (currentUserAction != null || currentUser != null) {
            Logger.error("startAction currentAction is not null");
            return;
        }

//        if (!isIoBoardOk(ioBoard.getStatus())) {
//            modelError.setError(ModelError.ERROR_CODE.BAG_NOT_INPLACE, "Bag not in place");
//            return;
//        }

        if (modelError.isError()) {
            Logger.info("Can't start an action when on error");
            return;
        }
        LgBag currentBag = LgBag.getCurrentBag();
        F.T5<Long, Long, Long, Map<Currency, LgDeposit.Total>, Map<Currency, Map<LgBillType, Bill>>> totals = currentBag.getTotals();
        if (Configuration.isBagFull(totals._3, totals._2)) {
            modelError.setError(ModelError.ERROR_CODE.BAG_FULL, "Bag full too many bills and evenlopes");
            return;
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
        if (manager.getStatus().getState() == ManagerInterface.MANAGER_STATE.ERROR) {
            if (!Configuration.isGloryIgnore()) {
                modelError.setError(ModelError.ERROR_CODE.APP_ERROR, String.format("Glory error %s", manager.getStatus()));
            }
        }

        IoBoard.IoBoardStatus status = ioBoard.getStatus();
        if (status != null && status.getError() != null) {
            if (!Configuration.isIoBoardIgnore()) {
                Logger.error("Setting ioboard error : %s", status.getError());
                modelError.setError(status.getError());
            }
        }

        if (modelError.isError()) {
            finishAction();
            return "ERROR";
        }
        if (currentUserAction
                != null) {
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

    private static void clearError() {
        Logger.debug("clearing error");
        ManagerInterface.MANAGER_STATE m = manager.getStatus().getState();
        if (m != ManagerInterface.MANAGER_STATE.ERROR && ioBoard.getError() == null) {
            modelError.clearError();
        }
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

    public static boolean isIoBoardOk(IoBoard.IoBoardStatus status) {
        if (status != null && status.getError() != null) {
            Logger.error("Setting ioboard error : %s", status.getError());
            modelError.setError(status.getError());
            return false;
        }
        if (!Configuration.isIgnoreBag()
                && status.getBagAproveState() != IoBoard.BAG_APROVE_STATE.BAG_APROVED) {
            Logger.error("IoBoard bag not inplace can't store");
            //modelError.setError(ModelError.ERROR_CODE.BAG_NOT_INPLACE, "bag not in place");
            return false;
        }
        return true;
    }

    public static Printer.PrinterStatus getPrinterStatus() {
        return printer.getStatus();
    }

    public static void print(String templateName, Map<String, Object> args, int paperWidth, int paperLen) {
        printer.print(templateName, args, paperWidth, paperLen);
    }

    public static ManagerInterface getGloryManager() {
        return manager;
    }

    public static IoBoard getIoBoard() {
        //Play.configuration.getProperty("io_board.port")
        return ioBoard;
    }

    public static Printer getPrinter() {
        return printer;
    }

    public static Glory getCounter() {
        return manager.getCounter();
    }

    public static Object getPrinters() {
        return printer.printers.values();
    }
}
