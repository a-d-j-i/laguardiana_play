/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.ManagerInterface;
import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.ioboard.IoBoard;
import devices.printer.Printer;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import models.ItemQuantity;
import models.ModelError;
import models.ModelFacade.UserActionApi;
import models.actions.states.ActionState;
import models.db.LgBag;
import models.db.LgBatch;
import models.db.LgBill;
import models.db.LgDeposit;
import models.db.LgDeposit.FinishCause;
import models.db.LgUser;
import models.events.TimeoutEvent;
import models.lov.Currency;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class UserAction {

    final protected Object actionData;
    final protected Currency currency;
    protected UserActionApi userActionApi = null;
    protected LgUser currentUser = null;
    protected ActionState state = null;
    protected Integer currentDepositId = null;
    protected Integer currentBatchId = null;

    public UserAction(Currency currency, Object formData) {
        this.actionData = formData;
        this.currency = currency;
    }

    public class StateApi {

        final private TimeoutTimer timer;

        public StateApi() {
            timer = new TimeoutTimer(UserAction.this);
        }

        public void setState(ActionState state) {
            Logger.debug("Setting imDone to false, state transition to %s", state.toString());
            imDone.set(false);
            UserAction.this.state = state;
        }

        public void count() {
            userActionApi.count(currency.numericId);
        }

        public void cancelDeposit() {
            userActionApi.cancelDeposit();
        }

        public boolean store() {
            return userActionApi.store(currentDepositId);
        }

        public boolean isIoBoardOk() {
            return userActionApi.isIoBoardOk();
        }

        public void withdraw() {
            userActionApi.withdraw();
        }

        public Iterable<LgBill> getCurrentBillList() {
            return userActionApi.getCurrentBillList();
        }

        public ItemQuantity getCurrentItemQuantity() {
            LgBag currentBag = LgBag.getCurrentBag();
            return currentBag.getItemQuantity(currentDepositId);
        }

        public void addBatchToDeposit(Map<Integer, Integer> bills) {
            LgDeposit deposit = LgDeposit.findById(currentDepositId);
            LgBatch batch = new LgBatch();
            for (LgBill bill : userActionApi.getCurrentBillList(bills)) {
                Logger.debug("%s -> quantity %d", bill.billType.toString(), bill.quantity);
                batch.addBill(bill);
            }
            deposit.addBatch(batch);
            batch.save();
            deposit.save();
            currentBatchId = batch.batchId;
        }

        public void closeBatch() {
            if (currentBatchId != null) {
                LgBatch b = LgBatch.findById(currentBatchId);
                if (b == null) {
                    Logger.error("current batch is null, batch id %d", currentBatchId);
                } else {
                    if (b.finishDate == null) {
                        b.finishDate = new Date();
                        b.save();
                    }
                }
            }
        }

        public void closeDeposit(FinishCause finishCause) {
            if (finishCause == FinishCause.FINISH_CAUSE_OK) {
                closeBatch();
            }
            Logger.debug("Closing deposit finish cause : %s", finishCause.name());
            LgDeposit d = LgDeposit.findById(currentDepositId);
            d.finishCause = finishCause;
            d.closeDate = new Date();
            d.save();
        }

        public void startTimer() {
            timer.start();
        }

        public void restartTimer() {
            timer.restart();
        }

        public void cancelTimer() {
            timer.cancel();
        }

        public void setError(ModelError.ERROR_CODE errorCode, String detail) {
            userActionApi.setError(errorCode, detail);
        }

        public void openGate() {
            userActionApi.openGate();
        }

        public void closeGate() {
            userActionApi.closeGate();
        }

        public ManagerInterface.MANAGER_STATE getManagerState() {
            return userActionApi.getManagerState();
        }

    }

    public void start(LgUser currentUser, UserActionApi userActionApi) {
        // Close any old unfinished deposit.
        LgDeposit.closeUnfinished();

        this.userActionApi = userActionApi;
        this.currentUser = currentUser;
        start();
    }

    public LgUser getCurrentUser() {
        return currentUser;
    }

    public Object getFormData() {
        return actionData;
    }
    // There is a race condition when in the same state they press accept and cancel at the same time
    // this is to avoid that.
    final private AtomicBoolean imDone = new AtomicBoolean(false);

    public void accept() {
        if (imDone.compareAndSet(false, true)) {
            state.accept();
        } else {
            Logger.debug("UserAction accept imDone");
        }
    }

    public void cancel() {
        if (imDone.compareAndSet(false, true)) {
            state.cancel();
        } else {
            Logger.debug("UserAction accept imDone");
        }
    }

    public boolean canFinishAction() {
        return state.canFinishAction();
    }

    public void suspendTimeout() {
        Logger.debug("--------------> SUSPEND");
        state.suspendTimeout();
    }

    abstract public void start();

    abstract public void finish();

    public void onGloryEvent(ManagerStatus m) {
        ActionState currState = state;
        do {
            Logger.debug("Action : OnGloryEvent state %s currState %s event %s",
                    state.getClass().getSimpleName(), currState.getClass().getSimpleName(), m.toString());
            currState = state;
            currState.onGloryEvent(m);
        } while (!state.equals(currState));
    }

    public void onIoBoardEvent(IoBoard.IoBoardStatus s) {
        ActionState currState = state;
        do {
            Logger.debug("Action : onIoBoardEvent state %s currState %s event %s",
                    state.getClass().getSimpleName(), currState.getClass().getSimpleName(), s.toString());
            currState = state;
            currState.onIoBoardEvent(s);
        } while (!state.equals(currState));
    }

    public void onPrinterEvent(Printer.PrinterStatus p) {
        ActionState currState = state;
        do {
            Logger.debug("Action : onPrinterEvent state %s currState %s event %s",
                    state.getClass().getSimpleName(), currState.getClass().getSimpleName(), p.toString());
            currState = state;
            state.onPrinterEvent(p);
        } while (!state.equals(currState));
    }

    public void onTimeoutEvent(TimeoutTimer timer) {
        Date currentDate = new Date();
        TimeoutEvent.save(this, currentDate.toString());
        state.onTimeoutEvent(timer);
    }

    abstract public String getNeededController();

    public String getStateName() {
        return state.name();
    }

    public String getNeededAction() {
        return state.getNeededActionAction();
    }

    public String getMessage() {
        return state.getMessage(this);
    }

    public Integer getDepositId() {
        return currentDepositId;
    }
}
