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
import models.Bill;
import models.ModelError;
import models.ModelFacade.UserActionApi;
import models.User;
import models.actions.states.ActionState;
import models.db.LgBatch;
import models.db.LgBill;
import models.db.LgDeposit;
import models.events.TimeoutEvent;
import models.lov.Currency;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class UserAction {

    final protected Object formData;
    final protected Currency currency;
    protected UserActionApi userActionApi = null;
    protected User currentUser = null;
    protected ActionState state = null;
    protected Integer currentDepositId = null;
    protected Integer currentBatchId = null;

    public UserAction(Currency currency, Object formData) {
        this.formData = formData;
        this.currency = currency;
    }

    public class StateApi {

        final private TimeoutTimer timer;

        public StateApi() {
            timer = new TimeoutTimer(UserAction.this);
        }

        public void setState(ActionState state) {
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

        public void addBatchToDeposit() {
            LgDeposit deposit = LgDeposit.findById(currentDepositId);
            LgBatch batch = new LgBatch();
            for (Bill bill : Bill.getBillList(currency.numericId)) {
                Logger.debug(" -> quantity %d", bill.q);
                LgBill b = new LgBill(bill.q, bill.billType);
                batch.addBill(b);
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

        public void closeDeposit() {
            closeBatch();
            LgDeposit d = LgDeposit.findById(currentDepositId);
            d.finishDate = new Date();
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

    public void start(User currentUser, UserActionApi userActionApi) {
        this.userActionApi = userActionApi;
        this.currentUser = currentUser;
        start();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Object getFormData() {
        return formData;
    }

    public void accept() {
        state.accept();
    }

    public void cancel() {
        state.cancel();
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
        } while (state != currState);
    }

    public void onIoBoardEvent(IoBoard.IoBoardStatus s) {
        ActionState currState = state;
        do {
            Logger.debug("Action : onIoBoardEvent state %s currState %s event %s",
                    state.getClass().getSimpleName(), currState.getClass().getSimpleName(), s.toString());
            currState = state;
            currState.onIoBoardEvent(s);
        } while (state != currState);
    }

    public void onPrinterEvent(Printer.PrinterStatus p) {
        ActionState currState = state;
        do {
            Logger.debug("Action : onPrinterEvent state %s currState %s event %s",
                    state.getClass().getSimpleName(), currState.getClass().getSimpleName(), p.toString());
            currState = state;
            state.onPrinterEvent(p);
        } while (state != currState);
    }

    public void onTimeoutEvent(TimeoutTimer timer) {
        Date currentDate = new Date();
        TimeoutEvent.save(this, currentDate.toString());
        state.onTimeoutEvent(timer);
    }

    public Currency getCurrency() {
        return currency;
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
