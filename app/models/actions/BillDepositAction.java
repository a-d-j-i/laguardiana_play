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
import models.db.LgBatch;
import models.db.LgBill;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;

/**
 *
 * @author adji
 */
public class BillDepositAction extends UserAction {

    static final EnumMap<GloryManager.Status, String> messageMap = new EnumMap<GloryManager.Status, String>(GloryManager.Status.class);

    static {
        messageMap.put(GloryManager.Status.READY_TO_STORE, "bill_deposit.ready_to_store");
        messageMap.put(GloryManager.Status.PUT_THE_BILLS_ON_THE_HOPER, "counting_page.put_the_bills_on_the_hoper");
        messageMap.put(GloryManager.Status.ESCROW_FULL, "bill_deposit.escrow_full");
        messageMap.put(GloryManager.Status.REMOVE_THE_BILLS_FROM_ESCROW, "counting_page.remove_the_bills_from_escrow");
        messageMap.put(GloryManager.Status.REMOVE_REJECTED_BILLS, "counting_page.remove_rejected_bills");
        messageMap.put(GloryManager.Status.REMOVE_THE_BILLS_FROM_HOPER, "counting_page.remove_the_bills_from_hoper");
        messageMap.put(GloryManager.Status.CANCELING, "counting_page.canceling");
        messageMap.put(GloryManager.Status.CANCELED, "counting_page.deposit_canceled");
        messageMap.put(GloryManager.Status.ERROR, "application.error");
    }
    public DepositUserCodeReference userCodeLov;
    public String userCode;

    public BillDepositAction(DepositUserCodeReference userCodeLov,
            String userCode, Currency currency, Object formData, int timeout) {
        super(currency, formData, messageMap, timeout);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
    }

    @Override
    final public String getActionNeededController() {
        return "BillDepositController";
    }

    @Override
    public void start() {
        Deposit deposit = new Deposit(currentUser, userCode, userCodeLov);
        deposit.save();
        currentDepositId = deposit.depositId;
        userActionApi.count(currency.numericId);
        startTimer();
    }

    @Override
    public void cancel() {
        if (currentDepositId == null) {
            Logger.error("cancelDeposit Invalid step %s", state.name());
            return;
        }
        state = ActionState.CANCELING;
        if (!userActionApi.cancelDeposit()) {
            Logger.error("cancelDeposit can't cancel glory");
        }
    }

    @Override
    public void accept() {
        if ((state != ActionState.READY_TO_STORE && state != ActionState.ESCROW_FULL)
                || currentDepositId == null) {
            Logger.error("acceptDeposit Invalid step");
            return;
        }
        Deposit deposit = Deposit.findById(currentDepositId);
        LgBatch batch = new LgBatch();
        for (Bill bill : Bill.getBillList(currency.numericId)) {
            Logger.debug(" -> quantity %d", bill.q);
            LgBill b = new LgBill(bill.q, bill.billType);
            batch.addBill(b);
        }
        deposit.addBatch(batch);
        batch.save();
        deposit.startDate = new Date();
        deposit.save();
        if (state == ActionState.READY_TO_STORE) {
            state = ActionState.READY_TO_STORE_STORING;
        } else if (state == ActionState.ESCROW_FULL) {
            state = ActionState.ESCROW_FULL_STORING;
        }
        if (!userActionApi.store(currentDepositId)) {
            Logger.error("startBillDeposit can't cancel glory");
        }

    }

    @Override
    public void onGloryEvent(GloryManager.Status m) {
        Logger.debug("BillDepositAction When Done %s %s", m.name(), state.name());
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
                if (m != GloryManager.Status.CANCELED) {
                    Logger.error("CANCELING Invalid manager status %s", m.name());
                    currentDepositId = null;
                } else {
                    state = ActionState.FINISH;
                    currentDepositId = null;
                }
                break;
            case ESCROW_FULL_STORING:
                if (m != GloryManager.Status.IDLE) {
                    Logger.error("ESCROW_FULL Invalid manager status %s", m.name());
                }
                state = ActionState.IDLE;
                break;
            case READY_TO_STORE_STORING:
                if (m != GloryManager.Status.IDLE) {
                    Logger.error("READY_TO_STORE Invalid manager status %s", m.name());
                }
                state = ActionState.FINISH;
                Deposit d = Deposit.findById(currentDepositId);
                d.finishDate = new Date();
                d.save();
                break;
            case FINISH:
                if (m != GloryManager.Status.IDLE) {
                    Logger.error("WhenDone invalid status %s %s", state.name(), m.name());
                }
                break;
            default:
                Logger.error("WhenDone invalid status %s %s", state.name(), m.name());
                currentDepositId = null;
                break;
        }
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.debug("CountingAction ioBoardEvent %s %s", status.status.name(), state.name());
    }

    @Override
    public void onTimeoutEvent() {
        Logger.debug("CountingAction timeoutEvent");
    }
}
