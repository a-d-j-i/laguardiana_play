/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.IoBoard;
import devices.glory.manager.ManagerInterface;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import models.Bill;
import models.ModelFacade.UserActionApi;
import models.TimeoutEvent;
import models.User;
import models.actions.states.ActionState;
import models.db.LgBatch;
import models.db.LgBill;
import models.db.LgDeposit;
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
    protected final Map<ManagerInterface.State, String> messages = new EnumMap<ManagerInterface.State, String>(ManagerInterface.State.class);
    protected ActionState state = null;
    protected Integer currentDepositId = null;

    public UserAction(Currency currency, Object formData, Map<ManagerInterface.State, String> msgs) {
        this.formData = formData;
        this.currency = currency;
        messages.put(ManagerInterface.State.PUT_THE_BILLS_ON_THE_HOPER, "counting_page.put_the_bills_on_the_hoper");
        messages.put(ManagerInterface.State.REMOVE_THE_BILLS_FROM_ESCROW, "counting_page.remove_the_bills_from_escrow");
        messages.put(ManagerInterface.State.REMOVE_REJECTED_BILLS, "counting_page.remove_rejected_bills");
        messages.put(ManagerInterface.State.REMOVE_THE_BILLS_FROM_HOPER, "counting_page.remove_the_bills_from_hoper");
        messages.put(ManagerInterface.State.CANCELING, "application.canceling");
        messages.put(ManagerInterface.State.CANCELED, "counting_page.deposit_canceled");
        messages.put(ManagerInterface.State.ERROR, "application.error");
        messages.put(ManagerInterface.State.JAM, "application.jam");
        for (Map.Entry<ManagerInterface.State, String> m : messages.entrySet()) {
            messages.put(m.getKey(), m.getValue());
        }
    }

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

        public void envelopeDeposit() {
            userActionApi.envelopeDeposit();
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
        }

        public void openDeposit() {
            LgDeposit d = LgDeposit.findById(currentDepositId);
            d.startDate = new Date();
            d.save();
        }

        public void closeDeposit() {
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

        public void setError(String msg) {
            userActionApi.setError(msg);
        }

        public void clearError() {
            userActionApi.clearError();
        }

        public void openGate() {
            userActionApi.openGate();
        }

        public void closeGate() {
            userActionApi.closeGate();
        }
    }

    public String getStateName() {
        return state.name();
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

    public void onGloryEvent(ManagerInterface.Status m) {
        state.onGloryEvent(m);
    }

    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        state.onIoBoardEvent(status);
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
