/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.Manager;
import java.util.Date;
import java.util.List;
import models.Deposit;
import models.db.LgBatch;
import models.db.LgBill;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.libs.F;
import validation.Bill;

/**
 *
 * @author adji
 */
public class BillDepositAction extends UserAction {

    static public enum ActionState {

        IDLE,
        ERROR,
        READY_TO_STORE,
        ESCROW_FULL,
        FINISH,
        CANCELING,;
    };
    public Deposit currentDeposit = null;
    public DepositUserCodeReference userCodeLov;
    public String userCode;
    public Currency currency;
    public ActionState state = ActionState.IDLE;

    public BillDepositAction(DepositUserCodeReference userCodeLov,
            String userCode, Currency currency, Object formData) {
        super(formData);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
        this.currency = currency;
    }

    @Override
    public String getControllerAction() {
        switch (state) {
            case ERROR:
                return "counterError";
            case FINISH:
                return "finish";
            default:
                return "mainLoop";
        }
    }

    @Override
    public F.Tuple<String, String> getActionState() {
        if (state == ActionState.IDLE) {
            switch (userActionApi.getManagerStatus()) {
                case ERROR:
                    state = ActionState.ERROR;
                    break;
                case READY_TO_STORE:
                    state = ActionState.READY_TO_STORE;
                    break;
                case ESCROW_FULL:
                    state = ActionState.ESCROW_FULL;
                    break;
                default:
                    Logger.debug("getControllerAction Current manager state %s %s",
                            state.name(), userActionApi.getManagerStatus().name());
                    break;
            }
        }
        return new F.Tuple<String, String>(state.name(), userActionApi.getManagerStatus().name());
    }

    @Override
    final public String getNeededController() {
        return "BillDepositController";
    }

    public void start() {
        currentDeposit = new Deposit(currentUser, userCode, userCodeLov, currency);
        currentDeposit.save();
        if (!userActionApi.count(currency.numericId)) {
            state = ActionState.ERROR;
            error = String.format("startBillDeposit can't start glory %s", userActionApi.getErrorDetail());
        }
    }

    public List<Bill> getBillData() {
        return Bill.getCurrentCounters(currency.numericId);
    }

    public Long getTotal() {
        if (currentDeposit == null) {
            return new Long(0);
        }
        return currentDeposit.getTotal();
    }

    public void acceptDeposit() {
        if ((state != ActionState.READY_TO_STORE
                && state != ActionState.ESCROW_FULL)
                || currentDeposit == null) {
            Logger.error("acceptDeposit Invalid step");
            return;
        }
        if (!userActionApi.storeDeposit(currentDeposit.depositId)) {
            Logger.error("startBillDeposit can't cancel glory");
        }
    }

    public void cancelDeposit() {
        if (currentDeposit == null) {
            Logger.error("cancelDeposit Invalid step %s", state.name());
            return;
        }
        state = ActionState.CANCELING;
        if (!userActionApi.cancelDeposit()) {
            Logger.error("cancelDeposit can't cancel glory");
        }
    }

    protected void error(String message, Object... args) {
        super.error(message, args);
        state = ActionState.ERROR;
    }

    @Override
    public void gloryDone(Manager.Status m, Manager.ErrorDetail me) {
        Logger.debug("BillDepositAction When Done %s %s", m.name(), state.name());
        switch (state) {
            case CANCELING:
                if (m != Manager.Status.CANCELED) {
                    error("CANCELING Invalid manager status %s", m.name());
                } else {
                    state = ActionState.FINISH;
                    currentDeposit = null;
                }
                break;
            case ESCROW_FULL:
                if (m != Manager.Status.IDLE) {
                    Logger.error("ESCROW_FULL Invalid manager status %s", m.name());
                }
                state = ActionState.IDLE;
                break;
            case READY_TO_STORE:
                if (m != Manager.Status.IDLE) {
                    Logger.error("READY_TO_STORE Invalid manager status %s", m.name());
                }
                state = ActionState.FINISH;
                currentDeposit.finishDate = new Date();
                break;
            default:
                error("WhenDone invalid status %s %s %s", state.name(), m.name(), me);
                return;
        }
        if (state == ActionState.ERROR) {
            currentDeposit = null;
            return;
        }

        Logger.debug("--------- esNewClasscrow full SAVE");
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
        currentDeposit.merge();
    }
}
