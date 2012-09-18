/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import controllers.Secure;
import devices.glory.manager.Manager;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import models.Deposit;
import models.db.LgBatch;
import models.db.LgBill;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import validation.Bill;

/**
 *
 * @author adji
 */
public class BillDepositAction extends UserAction {

    static public enum Action {

        IDLE,
        ERROR,
        READY_TO_STORE,
        ESCROW_FULL,
        FINISH,;
    }

    static public enum ActionMessage {

        IDLE(Action.IDLE, Manager.Status.IDLE),
        ERROR(Action.ERROR, Manager.Status.ERROR),
        READY_TO_STORE(Action.READY_TO_STORE, Manager.Status.READY_TO_STORE),
        ESCROW_FULL(Action.ESCROW_FULL, Manager.Status.ESCROW_FULL),
        FINISH(Action.FINISH, null),
        PUT_THE_BILLS_ON_THE_HOPER(Action.IDLE, Manager.Status.PUT_THE_BILLS_ON_THE_HOPER),
        REMOVE_THE_BILLS_FROM_HOPER(Action.IDLE, Manager.Status.REMOVE_THE_BILLS_FROM_HOPER),
        REMOVE_THE_BILLS_FROM_ESCROW(Action.IDLE, Manager.Status.REMOVE_THE_BILLS_FROM_ESCROW),
        REMOVE_REJECTED_BILLS(Action.IDLE, Manager.Status.REMOVE_REJECTED_BILLS),
        CANCELING(Action.IDLE, Manager.Status.CANCELING),;
        final private Action action;
        final private Manager.Status m;
        static final EnumMap< Manager.Status, ActionMessage> reverse = new EnumMap< Manager.Status, ActionMessage>(Manager.Status.class);

        static {
            for (ActionMessage s : ActionMessage.values()) {
                if (s.m != null) {
                    reverse.put(s.m, s);
                }
            }
        }

        private ActionMessage(Action action, Manager.Status m) {
            this.action = action;
            this.m = m;
        }

        public Action getCurrentStep() {
            return action;
        }

        static public ActionMessage getByManagerStatus(Manager.Status ms) {
            if (!reverse.containsKey(ms)) {
                return null;
            }
            return reverse.get(ms);
        }
    };
    public Deposit currentDeposit = null;
    public DepositUserCodeReference userCodeLov;
    public String userCode;
    public Currency currency;

    public BillDepositAction(DepositUserCodeReference userCodeLov, 
                        String userCode, Currency currency, Object formData) {
        super(formData);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
        this.currency = currency;
    }

    public Action getAction() {
        ActionMessage m = getMessage();
        if (m == null) {
            return null;
        } else {
            return m.action;
        }
    }

    public String getMessageName() {
        ActionMessage m = getMessage();
        if (m == null) {
            return "none";
        } else {
            return m.name();
        }
    }

    public ActionMessage getMessage() {
        switch (getCurrentStep()) {
            case FINISH:
                return ActionMessage.FINISH;
            case ERROR:
                return ActionMessage.ERROR;
            case RUNNING:
                Manager.Status m = userActionApi.getManagerStatus();
                return ActionMessage.getByManagerStatus(m);
            case NONE:
            case RESERVED:
            default:
                return null;
        }
    }

    @Override
    final public String getNeededController() {
        return "BillDepositController";
    }

    public void start() {
        currentDeposit = new Deposit(Secure.getCurrentUser(), userCode, userCodeLov, currency);
        currentDeposit.save();
        Deposit.em().getTransaction().commit();
        Deposit.em().getTransaction().begin();
        currentStep = CurrentStep.RUNNING;
        if (!userActionApi.count(currency.numericId)) {
            currentStep = CurrentStep.ERROR;
            error = "startBillDeposit can't start glory";
        }
    }

    public List<Bill> getBillData() {
        return Bill.getCurrentCounters(currency.numericId);
    }

    public Long getTotal() {
        return currentDeposit.getTotal();
    }

    public void acceptDeposit() {
        if (getCurrentStep() != CurrentStep.RUNNING || currentDeposit == null) {
            Logger.error("cancelBillDeposit Invalid step");
            return;
        }
        if (!userActionApi.storeDeposit(currentDeposit.depositId)) {
            Logger.error("startBillDeposit can't cancel glory");
        }
    }

    public void cancelDeposit() {
        CurrentStep c = getCurrentStep();
        if (currentDeposit == null || c != CurrentStep.RUNNING) {
            Logger.error("cancelDeposit Invalid step %s", c.name());
            return;
        }
        if (!userActionApi.cancelDeposit()) {
            Logger.error("cancelDeposit can't cancel glory");
        }
    }

    // TODO: Event dispatch routine...
    @Override
    public void gloryDone(Manager.Status m, Manager.ErrorDetail me) {
        Logger.debug("BillDepositAction When Done %s %s", m.name(), currentStep.name());
        if (currentStep != CurrentStep.RUNNING && currentStep != CurrentStep.ERROR) {
            // TODO: Log the event.
            Logger.error("WhenDone not running");
            currentStep = CurrentStep.ERROR;
            return;
        }
        currentStep = CurrentStep.FINISH;
        switch (m) {
            case CANCELED:
                if (Deposit.em().getTransaction().isActive()) {
                    Deposit.em().getTransaction().rollback();
                }
                currentDeposit = null;
                break;

            case IDLE:
                Logger.debug("--------- idle SAVE");
                addBatchToDeposit();
                currentDeposit.finishDate = new Date();
                currentDeposit.merge();
                Logger.debug("--------- presave");
                if (Deposit.em().getTransaction().isActive()) {
                    Deposit.em().getTransaction().commit();
                    Deposit.em().getTransaction().begin();
                }
                break;
            case STORING: // ESCROW_FULL
                Logger.debug("--------- esNewClasscrow full SAVE");
                addBatchToDeposit();
                currentDeposit.merge();
                if (Deposit.em().getTransaction().isActive()) {
                    Deposit.em().getTransaction().commit();
                    Deposit.em().getTransaction().begin();
                }
                currentStep = CurrentStep.RUNNING;
                break;
            case ERROR:
                Logger.error("WhenDone invalid machine error %s", me);
                currentStep = CurrentStep.ERROR;
                break;
            default:
                Logger.error("WhenDone invalid machine status %s", m);
                currentStep = CurrentStep.ERROR;
                break;
        }
    }

// TODO: Manage the db error here.
    private void addBatchToDeposit() {
        if (currentDeposit == null) {
            Logger.error("addBatchToDeposit current deposit is null");
            return;
        }
        LgBatch batch = new LgBatch();
        for (Bill bill : Bill.getCurrentCounters(currentDeposit.currency.numericId)) {
            Logger.debug(" -> quantity %d", bill.q);
            LgBill b = new LgBill(bill.q, bill.billType);
            batch.addBill(b);
        }
        currentDeposit.addBatch(batch);
        batch.save();
    }
}
