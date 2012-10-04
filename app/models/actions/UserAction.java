/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.IoBoard;
import devices.glory.manager.GloryManager;
import java.util.Date;
import java.util.EnumMap;
import models.Bill;
import models.Deposit;
import models.Event;
import models.ModelFacade.UserActionApi;
import models.User;
import models.actions.states.ActionState;
import models.actions.states.Finish;
import models.db.LgBatch;
import models.db.LgBill;
import models.lov.Currency;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.libs.F.Promise;

/**
 *
 * @author adji
 */
abstract public class UserAction {

  
    final protected Object formData;
    final protected Currency currency;
    protected UserActionApi userActionApi = null;
    protected User currentUser = null;
    protected final EnumMap<GloryManager.Status, String> messages;
    protected ActionState state = null;
    protected Integer currentDepositId = null;
    protected Timeout timer = null;

    public class StateApi {

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

        public void cancelTimer() {
            UserAction.this.cancelTimer();
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
    }

    public UserAction(Currency currency, Object formData, EnumMap<GloryManager.Status, String> messages) {
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

    abstract public void start();

    // TODO: Need events ???
    public void onGloryEvent(GloryManager.Status m) {
        state.onGloryEvent(m);
    }

    // TODO: Need events ???
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        state.onIoBoardEvent(status);
    }

    // TODO: Need events ???
    public void onTimeoutEvent(Timeout timeout, ActionState startState) {
        state.onTimeoutEvent(timeout, startState);
    }

    abstract public String getActionNeededController();

    public Currency getCurrency() {
        return currency;
    }

    public String getNeededActionAction() {
        if (state instanceof Finish) {
            return "finish";
        } else {
            return "mainLoop";
        }
    }

    public String getActionMessage() {
        return messages.get(userActionApi.getManagerStatus());
    }

    public Integer getDepositId() {
        return currentDepositId;
    }

    public void finishAction() {
        cancelTimer();
    }

    synchronized protected void startTimer(ActionState startState) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timeout(startState);
        timer.startWarnTimeout();
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        } else {
            Logger.error("Trying to cancel an invalid timer");
        }
    }

    public class Timeout extends Job {

        private int timeout1 = 10;
        private int timeout2 = 60;
        final public ActionState startState;
        public Promise promise;

        public Timeout(ActionState startState) {
            this.startState = startState;
            try {
                timeout1 = Integer.parseInt(Play.configuration.getProperty("timer.timeout1"));
            } catch (NumberFormatException e) {
                Logger.debug("Error parsing timer.timeout1 %s", e.getMessage());
            }
            try {
                timeout2 = Integer.parseInt(Play.configuration.getProperty("timer.timeout2"));
            } catch (NumberFormatException e) {
                Logger.debug("Error parsing timer.timeout1 %s", e.getMessage());
            }
        }

        public void startWarnTimeout() {
            promise = this.in(timeout1);
        }

        public void startCancelTimeout() {
            promise = this.in(timeout2);
        }

        public void cancel() {
            promise.cancel(true);
        }

        @Override
        public void doJob() {
            Date currentDate = new Date();
            Event.save(getDepositId(), Event.Type.TIMEOUT, currentDate.toString());
            onTimeoutEvent(this, startState);
        }
    }
}
