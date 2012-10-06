/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.IoBoard;
import devices.glory.manager.GloryManager;
import java.util.Date;
import java.util.Map;
import models.Bill;
import models.Deposit;
import models.Event;
import models.ModelFacade.UserActionApi;
import models.User;
import models.actions.states.ActionState;
import models.db.LgBatch;
import models.db.LgBill;
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
    protected final Map<GloryManager.State, String> messages;
    protected ActionState state = null;
    protected Integer currentDepositId = null;

    public class StateApi {

        final private TimeoutTimer timer;

        public StateApi() {
            timer = new TimeoutTimer(UserAction.this);
        }

        public void setState(ActionState state) {
            UserAction.this.state = state;
        }

        public boolean cancelDeposit() {
            return userActionApi.cancelDeposit();
        }

        public boolean store() {
            return userActionApi.store(currentDepositId);
        }

        public void withdraw() {
            userActionApi.withdraw();
        }

        public void addBatchToDeposit() {
            Deposit deposit = Deposit.findById(currentDepositId);
            LgBatch batch = new LgBatch();
            for (Bill bill : Bill.getBillList(currency.numericId)) {
                Logger.debug(" -> quantity %d", bill.q);
                LgBill b = new LgBill(bill.q, bill.billType);
                batch.addBill(b);
            }
            deposit.addBatch(batch);
            batch.save();
            deposit.save();
        }

        public void openDeposit() {
            Deposit d = Deposit.findById(currentDepositId);
            d.startDate = new Date();
            d.save();
        }

        public void closeDeposit() {
            Deposit d = Deposit.findById(currentDepositId);
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

        public void setError(String msg) {
            userActionApi.setError(msg);
        }

        public void clearError() {
            userActionApi.clearError();
        }
    }

    public UserAction(Currency currency, Object formData, Map<GloryManager.State, String> messages) {
        this.formData = formData;
        this.messages = messages;
        this.currency = currency;
    }

    public String getStateName() {
        return state.name();
    }

    public void start(User currentUser, UserActionApi userActionApi) {
        this.userActionApi = userActionApi;
        this.currentUser = currentUser;
        start();
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

    public void onGloryEvent(GloryManager.Status m) {
        state.onGloryEvent(m);
    }

    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        state.onIoBoardEvent(status);
    }

    public void onTimeoutEvent(TimeoutTimer timer) {
        Date currentDate = new Date();
        Event.save(this, Event.Type.TIMEOUT, currentDate.toString());
        state.onTimeoutEvent(timer);
    }

    public Currency getCurrency() {
        return currency;
    }

    abstract public String getNeededController();

    public String getNeededAction() {
        return state.getNeededActionAction();
    }

    public String getMessage() {
        return messages.get(userActionApi.getManagerState());
    }

    public Integer getDepositId() {
        return currentDepositId;
    }
}
