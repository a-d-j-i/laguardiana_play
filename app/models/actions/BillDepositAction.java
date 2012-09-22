/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

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

    static final EnumMap<Manager.Status, String> messageMap = new EnumMap<Manager.Status, String>(Manager.Status.class);

    static {
        messageMap.put(Manager.Status.READY_TO_STORE, "bill_deposit.ready_to_store");
        messageMap.put(Manager.Status.PUT_THE_BILLS_ON_THE_HOPER, "counting_page.put_the_bills_on_the_hoper");
        messageMap.put(Manager.Status.ESCROW_FULL, "bill_deposit.escrow_full");
        messageMap.put(Manager.Status.REMOVE_THE_BILLS_FROM_ESCROW, "counting_page.remove_the_bills_from_escrow");
        messageMap.put(Manager.Status.REMOVE_REJECTED_BILLS, "counting_page.remove_rejected_bills");
        messageMap.put(Manager.Status.REMOVE_THE_BILLS_FROM_HOPER, "counting_page.remove_the_bills_from_hoper");
        messageMap.put(Manager.Status.CANCELING, "counting_page.canceling");
        messageMap.put(Manager.Status.CANCELED, "counting_page.deposit_canceled");
        messageMap.put(Manager.Status.ERROR, "application.error");
    }
    public DepositUserCodeReference userCodeLov;
    public String userCode;
    public Currency currency;

    public BillDepositAction(DepositUserCodeReference userCodeLov,
            String userCode, Currency currency, Object formData) {
        super(formData, messageMap);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
        this.currency = currency;
    }

    @Override
    final public String getNeededController() {
        return "BillDepositController";
    }

    @Override
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
        if ((state != ActionState.READY_TO_STORE && state != ActionState.ESCROW_FULL)
                || currentDeposit == null) {
            Logger.error("acceptDeposit Invalid step");
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

    @Override
    public void gloryDone(Manager.Status m, Manager.ErrorDetail me) {
        Logger.debug("BillDepositAction When Done %s %s", m.name(), state.name());
        if (m == Manager.Status.ERROR) {
            error("Glory Error : %s", me);
            return;
        }
        switch (state) {
            case IDLE:
                switch (m) {
                    case READY_TO_STORE:
                        state = ActionState.READY_TO_STORE;
                        break;
                    case ESCROW_FULL:
                        state = ActionState.ESCROW_FULL;
                        break;
                    case IDLE:
                        Logger.debug("Seting state to finish...");
                        state = ActionState.FINISH;
                        break;
                }
                break;
            case CANCELING:
                if (m != Manager.Status.CANCELED) {
                    error("CANCELING Invalid manager status %s", m.name());
                    currentDeposit = null;
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
                currentDeposit.merge();
                break;
            default:
                error("WhenDone invalid status %s %s %s", state.name(), m.name(), me);
                currentDeposit = null;
                break;
        }
    }
}
