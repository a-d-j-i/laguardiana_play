/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.IoBoard;
import devices.glory.manager.GloryManager;
import java.util.Date;
import java.util.EnumMap;
import models.Event;
import models.ModelFacade.UserActionApi;
import models.User;
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

    static public enum ActionState {

        IDLE,
        READY_TO_STORE,
        ESCROW_FULL,
        READY_TO_STORE_STORING,
        ESCROW_FULL_STORING,
        CONTINUE_DEPOSIT,
        FINISH,
        CANCELING,;
    };
    final protected Object formData;
    final protected Currency currency;
    protected UserActionApi userActionApi = null;
    protected User currentUser = null;
    protected final EnumMap<GloryManager.Status, String> messages;
    protected ActionState state = ActionState.IDLE;
    protected Integer currentDepositId = null;
    protected Timeout timer = null;

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

    abstract public void start();

    abstract public void cancel();

    abstract public void accept();

    abstract public void onGloryEvent(GloryManager.Status m);

    abstract public void onIoBoardEvent(IoBoard.IoBoardStatus status);

    abstract public String getActionNeededController();

    public Currency getCurrency() {
        return currency;
    }

    public String getNeededActionAction() {
        switch (state) {
            case FINISH:
                return "finish";
            default:
                return "mainLoop";
        }
    }

    public String getActionMessage() {
        return messages.get(userActionApi.getManagerStatus());
    }

    public Integer getDepositId() {
        return currentDepositId;
    }
//    protected void error(String message, Object... args) {
//        Logger.error(message, args);
//        error = String.format(message, args);
//        state = ActionState.ERROR;
//    }

    // TODO: Review what else I must do?.
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

    abstract public void cancelTimer();

    abstract public void onTimeoutEvent(Timeout timeout, ActionState startState);

    protected class Timeout extends Job {

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
