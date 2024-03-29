/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controllers.Secure;
import devices.DeviceFactoryEventJob;
import devices.DeviceFactory;
import devices.glory.Glory;
import devices.glory.manager.FakeGloryManager;
import devices.glory.manager.ManagerInterface;
import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.ioboard.IoBoard;
import devices.printer.Printer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.print.PrintService;
import jobs.PurgeDatabase;
import models.actions.UserAction;
import models.db.LgBag;
import models.db.LgBill;
import models.db.LgBillType;
import models.db.LgDeposit;
import models.db.LgUser;
import models.events.ActionEvent;
import models.events.GloryEvent;
import models.events.IoBoardEvent;
import models.events.PrinterEvent;
import play.Logger;
import play.db.jpa.JPA;
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

    final static private ManagerInterface manager;
    final static private IoBoard ioBoard;
    final static private ModelError modelError = new ModelError();
    final static private AtomicReference<Printer> printer = new AtomicReference<Printer>();
    static private UserAction currentUserAction = null;
    static private LgUser currentUser = null;

    static {
        if (Configuration.isIgnoreGlory()) {
            manager = new FakeGloryManager();
        } else {
            manager = DeviceFactory.getGloryManager(Configuration.getGloryPort());
        }
        manager.addObserver(new Observer() {
            public void update(Observable o, Object data) {
                Promise now = new OnGloryEvent((ManagerStatus) data).now();
            }
        });

        IoBoard.IOBOARD_VERSION ver = IoBoard.IOBOARD_VERSION.getVersion(Configuration.getIoBoardVersion());
        ioBoard = DeviceFactory.getIoBoard(Configuration.getIoBoardPort(), ver);
        ioBoard.addObserver(new Observer() {
            public void update(Observable o, final Object data) {
                Promise now = new OnIoBoardEvent((IoBoard.IoBoardStatus) data).now();
            }
        });

        setCurrentPrinter(null);
    }
    // TODO: Review

    public static void initialize() {
        // used to force the execution of the static code.
        // Close unifnished deposits.
        LgDeposit.closeUnfinished();
        Configuration.initCrapId();
        Logger.debug("APP VERSION : " + Configuration.getVersion());
    }

    interface BillListVisitor {

        public void visit(LgBillType billType, Integer desired, Integer current);
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
                case NEUTRAL:
                    if (modelError.getGloryError() != null) {
                        modelError.clearGloryError();
                    }
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
                        // let the action close the deposit.
                        u.onGloryEvent(status);
                    }
                    if (status.getError() != null) {
                        modelError.setError(status.getError());
                    }
                    break;
                case INITIALIZING:
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
    static int aproveRetry = 0;

    static class OnIoBoardEvent extends DeviceFactoryEventJob {

        IoBoard.IoBoardStatus status;

        public OnIoBoardEvent(IoBoard.IoBoardStatus status) {
            this.status = status;
        }

        @Override
        public void doJob() throws Exception {
            if (Configuration.isIgnoreIoBoard()) {
                return;
            }
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

            if (status.getError() != null) {
                if (u != null) {
                    u.cancel();
                }
                modelError.setError(status.getError());
                return;
            } else {
                modelError.clearIoBoardError();
            }
            Logger.debug("BAG STATUS : %s", status.toString());
            // I can't use the aproved state to rotate the bag because 
            // when the ioboard loose power the bug is not aproved but 
            // I don't want it to be rotated.
//            if (status.getBagState() != IoBoard.BAG_STATE.BAG_STATE_INPLACE || status.getBagAproveState() != IoBoard.BAG_APROVE_STATE.BAG_APROVED) {
            if (status.getBagState() != IoBoard.BAG_STATE.BAG_STATE_INPLACE) {
                // if bag not in place rotate current bag.
                ModelFacade.withdrawBag(true);
            }
            // Bag change.
            if (status.getBagState() == IoBoard.BAG_STATE.BAG_STATE_INPLACE) {
                switch (status.getBagAproveState()) {
                    case BAG_NOT_APROVED:
                        aproveRetry = 0;
                        ioBoard.aproveBag();
                        /*if (!manager.collect()) {
                         modelError.setError(ModelError.ERROR_CODE.ERROR_TRYING_TO_COLLECT, "error trying to collect");
                         }*/
                        break;
                    case BAG_APROVE_WAIT:
                        aproveRetry++;
                        if (aproveRetry > 5) {
                            Logger.debug("APROVE RETRY");
                            aproveRetry = 0;
                            ioBoard.aproveBag();
                        }
                        break;
                    case BAG_APROVED:
                        aproveRetry = 0;
                        // Bag aproved, recover from error.
                        if (modelError.getErrorCode() == ModelError.ERROR_CODE.BAG_NOT_INPLACE) {
                            errorReset();
                            //clearError();
                        }
                        break;
                    case BAG_APROVE_CONFIRM:
                        LgBag.placeBag();
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
                if (!Configuration.isIgnorePrinter()) {
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
                    setError(ModelError.ERROR_CODE.APPLICATION_ERROR, "count currentAction is null");
                    return;
                }
                if (!manager.count(null, numericId)) {
                    setError(ModelError.ERROR_CODE.APPLICATION_ERROR, "cant start count");
                }
            }
        }

        public boolean store(Integer depositId, boolean envelope) {
            if (!isBagReady(envelope)) {
                return false;
            }
            return manager.storeDeposit(depositId);
        }

        public void withdraw() {
            if (!manager.withdrawDeposit()) {
                setError(ModelError.ERROR_CODE.APPLICATION_ERROR, "cant start withdraw");
            }
        }

        public void cancelDeposit() {
            manager.cancelCommand();
        }

        public void envelopeDeposit() {
            synchronized (ModelFacade.class) {
                if (ModelFacade.currentUserAction == null) {
                    setError(ModelError.ERROR_CODE.APPLICATION_ERROR, "envelopeDeposit currentAction is null");
                    return;
                }
                if (!manager.envelopeDeposit()) {
                    setError(ModelError.ERROR_CODE.APPLICATION_ERROR, "cant start envelope deposit");
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

        public boolean isBagReady(boolean envelope) {
            return ModelFacade.isBagReady(envelope);
        }

        public List<LgBill> getCurrentBillList() {
            synchronized (ModelFacade.class) {
                Map<Integer, Integer> currentQuantity = null;
                if (manager != null) {
                    currentQuantity = manager.getCurrentQuantity();
                }
                return getCurrentBillList(currentQuantity);
            }
        }

        public List<LgBill> getCurrentBillList(final Map<Integer, Integer> currentQuantity) {
            synchronized (ModelFacade.class) {
                final List<LgBill> ret = new ArrayList<LgBill>();
                visitBillList(currentQuantity, new BillListVisitor() {
                    public void visit(LgBillType billType, Integer desired, Integer current) {
                        LgBill b = new LgBill(current, billType);
                        ret.add(b);
                    }
                });
                return ret;
            }
        }
    }

    synchronized public static boolean isError() {
        return modelError.isError();
    }

    synchronized public static void errorReset() {
        if (manager.getStatus().getError() != null) {
            manager.reset();
        }
        if (ioBoard.getStatus().getError() != null) {
            ioBoard.reset();
        }
        if (modelError.getErrorCode() != null) {
            modelError.clearErrorCodeError();
        }
    }

    synchronized public static void storingErrorReset() {
        manager.storingErrorReset();
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
        if (!isBagReady(false)) {
            Logger.info("Can't start bag not ready");
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
            if (!currentUserAction.canFinishAction()) {
                Logger.debug("CALLED FINISH ACTION BUT CAN FINISH " + currentUserAction.toString());
            }
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
        ManagerStatus mstate = manager.getStatus();
        if (mstate.getState() == ManagerInterface.MANAGER_STATE.ERROR) {
            if (!Configuration.isIgnoreGlory()) {
                modelError.setError(mstate.getError());
            }
        }

        IoBoard.IoBoardStatus status = ioBoard.getStatus();
        if (status != null && status.getError() != null) {
            if (!Configuration.isIgnoreIoBoard()) {
                Logger.error("Setting ioboard error : %s", status.getError());
                modelError.setError(status.getError());
            }
        }

        if (modelError.isError()) {
            finishAction();
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

    synchronized public static Object getLockedByUser() {
        LgUser loggedUser = Secure.getCurrentUser();
        if (currentUser != null && !currentUser.equals(loggedUser)) {
            return currentUser.username;
        }
        return null;
    }

    synchronized public static boolean isLocked() {
        LgUser loggedUser = Secure.getCurrentUser();
        if (loggedUser.isAdmin()) {
            return false;
        }
        if (currentUser != null && !currentUser.equals(loggedUser)) {
            return true;
        }
        return false;
    }

    synchronized public static Collection<BillQuantity> getBillQuantities() {
        Map<Integer, Integer> currentQuantity = null;
        if (manager != null) {
            currentQuantity = manager.getCurrentQuantity();
        }
        final SortedMap<BillValue, BillQuantity> ret = new TreeMap<BillValue, BillQuantity>();
        visitBillList(currentQuantity, new BillListVisitor() {
            public void visit(LgBillType billType, Integer desired, Integer current) {
                BillValue bv = billType.getValue();
                BillQuantity billQuantity = ret.get(bv);
                if (billQuantity == null) {
                    billQuantity = new BillQuantity(bv);
                }
                billQuantity.quantity += current;
                billQuantity.desiredQuantity += desired;
                ret.put(bv, billQuantity);

            }
        });
        return ret.values();
    }

    private static void visitBillList(Map<Integer, Integer> currentQuantity, BillListVisitor visitor) {
        Integer currency = manager.getCurrency();
        if (currency == null) {
            return;
        }

        List<LgBillType> billTypes = LgBillType.find(currency);

        Map<Integer, Integer> desiredQuantity = null;
        if (manager != null) {
            desiredQuantity = manager.getDesiredQuantity();
        }
        Set<Integer> slots = new HashSet();
        if (currentQuantity != null) {
            slots = new HashSet(currentQuantity.keySet());
        }
        for (LgBillType billType : billTypes) {
            Integer desired = 0;
            Integer current = 0;

            if (currentQuantity != null && currentQuantity.containsKey(billType.slot)) {
                slots.remove(billType.slot);
                current = currentQuantity.get(billType.slot);
            }
            if (desiredQuantity != null && desiredQuantity.containsKey(billType.slot)) {
                desired = desiredQuantity.get(billType.slot);
            }
            visitor.visit(billType, desired, current);
        }
        if (!slots.isEmpty()) {
            for (Integer s : slots) {
                if (currentQuantity != null && currentQuantity.get(s) > 0) {
                    modelError.setError(ModelError.ERROR_CODE.APPLICATION_ERROR,
                            String.format("The bill type slots must be configured correctly slot %d value %d", s, currentQuantity.get(s)));
                }
            }
        }
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
        Logger.debug("MODELFACADE: cancel");
        currentUserAction.cancel();
    }

    public static void accept() {
        if (currentUserAction == null) {
            Logger.error("accept invalid current User Action");
            return;
        }
        Logger.debug("MODELFACADE: accept");
        currentUserAction.accept();
    }

    public static void suspendTimeout() {
        if (currentUserAction == null) {
            Logger.error("cancelTimeout invalid current User Action");
            return;
        }
        currentUserAction.suspendTimeout();
    }

    public static ManagerInterface getGloryManager() {
        return manager;
    }

    public static IoBoard getIoBoard() {
        //Play.configuration.getProperty("io_board.port")
        return ioBoard;
    }

    public static Glory getCounter() {
        return manager.getCounter();
    }

    public static class IoBoardCondition {

        public final boolean ready;
        public final boolean error;
        public final boolean bag_removed;
        public final boolean gate1_ready;
        public final boolean gate2_ready;
        public final boolean door_ready;

        public IoBoardCondition(boolean error, boolean bag_removed, boolean gate1_ready, boolean gate2_ready, boolean door_ready) {
            this.error = error;
            this.bag_removed = bag_removed;
            this.gate1_ready = gate1_ready;
            this.gate2_ready = gate2_ready;
            this.door_ready = door_ready;
            this.ready = (!error && !bag_removed && gate1_ready && gate2_ready && door_ready);
        }

    }

    public static IoBoardCondition ioBoardReadyCondition() {
        IoBoard.IoBoardStatus status = ioBoard.getStatus();
        Logger.debug("CURRENT IOBOARD STATUS : %s", status.toString());
        if (Configuration.isIgnoreIoBoard() || status == null) {
            return new IoBoardCondition(false, false, true, true, true);
        }

        if (status.getError() != null) {
            Logger.error("Setting ioboard error : %s", status.getError());
            modelError.setError(status.getError());
            return new IoBoardCondition(true, false, true, true, true);
        }

        boolean bag_removed = false;
        if (!Configuration.isIgnoreBag() && status.getBagAproveState() != IoBoard.BAG_APROVE_STATE.BAG_APROVED) {
            //modelError.setError(ModelError.ERROR_CODE.BAG_NOT_INPLACE, "bag not in place");
            bag_removed = true;
        }
        return new IoBoardCondition(false, bag_removed,
                Configuration.isReadyGate1(status.getGateState()),
                Configuration.isReadyGate2(status.getGateState()),
                Configuration.isReadyGateDoor(status.getGateState()));
    }

    public static boolean ioBoardReady() {
        IoBoardCondition c = ioBoardReadyCondition();
        if (c.bag_removed) {
            Logger.error("IoBoard bag not inplace can't store");
        }
        if (!c.gate1_ready) {
            Logger.error("IoBoard gate1 not ready can't store");
        }
        if (!c.gate2_ready) {
            Logger.error("IoBoard gate2 not ready can't store");
        }
        if (!c.door_ready) {
            Logger.error("IoBoard gateDoor not ready can't store");
        }
        return c.ready;
    }

    public static boolean isBagReady(boolean envelope) {
        if (!ModelFacade.ioBoardReady()) {
            Logger.info("Can't start ioboard not ready");
            return false;
        }

        if (Configuration.isIgnoreBag()) {
            return true;
        }
        LgBag currentBag = LgBag.getCurrentBag();
        ItemQuantity iq = currentBag.getItemQuantity();
        // for an envelope deposit I neet at least space for one envelope more.
        if (envelope) {
            iq.envelopes++;
            iq.bills--;
        }
        Logger.debug("isBagReady quantity : %s", iq);
        if (Configuration.isBagFull(iq.bills, iq.envelopes)) {
            Logger.info("Can't start bag full");
            //modelError.setError(ModelError.ERROR_CODE.BAG_FULL, "Bag full too many bills and evenlopes");
            return false;
        }
        return true;
    }

    public static void withdrawBag(boolean byIoBoard) {
        LgBag currentBag = LgBag.getCurrentBag();
        if (currentBag == null) {
            Logger.error("CURRENT BAG IS NULL");
        } else {
            currentBag.withdrawBag(byIoBoard);
        }
    }

    public static void setCurrentPrinter(String prt) {
        if (prt == null) {
            prt = Configuration.getDefaultPrinter();
            if (prt == null) {
                Logger.error("Default printer must be configured");
                return;
            }
        }

        PrintService p = (PrintService) Printer.printers.get(prt);
        if (p == null) {
            Logger.error("Wrong printer name %s", prt);
            return;
        }
        Configuration.setDefaultPrinter(prt);
        Printer currPrinter = DeviceFactory.getPrinter(prt);
        currPrinter.addObserver(new Observer() {
            public void update(Observable o, Object data) {
                Promise now = new OnPrinterEvent((Printer.PrinterStatus) data).now();
            }
        });

        Printer oldPrinter = printer.getAndSet(currPrinter);
        if (oldPrinter != null) {
            //oldPrinter.close();
        }
    }

    public static Object getPrinters() {
        return Printer.printers.values();
    }

    public static boolean printerNeedCheck() {
        if (printer == null) {
            return true;
        }
        Printer p = printer.get();
        if (p == null) {
            return true;
        }
        return p.needCheck();
    }

    public static void print(String templateName, Map<String, Object> args, int paperWidth, int paperLen, boolean printTktNum, int copies) {
        print(null, templateName, args, paperWidth, paperLen, printTktNum, copies);
    }

    public static void print(String prt, String templateName, Map<String, Object> args, int paperWidth, int paperLen, boolean printTktNum, int copies) {
        if (prt == null) {
            prt = Configuration.getDefaultPrinter();
            if (prt == null) {
                Logger.error("Default printer must be configured");
                return;
            }
        }

        PrintService p = (PrintService) Printer.printers.get(prt);
        if (p == null) {
            Logger.error("Wrong printer name %s", prt);
            return;
        }
        Printer pp = DeviceFactory.getPrinter(prt);
        pp.print(templateName, args, paperWidth, paperLen, printTktNum, copies);
        if (pp != printer.get()) {
            //pp.close();
        }
    }

    public static Printer getCurrentPrinter() {
        return printer.get();
    }

}
