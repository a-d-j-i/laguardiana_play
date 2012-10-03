/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.IoBoard;
import devices.glory.manager.GloryManager;
import java.util.Date;
import java.util.EnumMap;
import java.util.Timer;
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
            String userCode, Currency currency, Object formData) {
        super(currency, formData, messageMap);
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
    }

    @Override
    public void cancel() {
        if (state != ActionState.READY_TO_STORE && state != ActionState.ESCROW_FULL) {
            Logger.error("cancel Invalid step");
        }
        cancelTimer();
        state = ActionState.CANCELING;
        if (!userActionApi.cancelDeposit()) {
            Logger.error("cancelDeposit can't cancel glory");
        }
    }

    @Override
    public void accept() {
        if (state != ActionState.READY_TO_STORE && state != ActionState.ESCROW_FULL) {
            Logger.error("acceptDeposit Invalid step");
            return;
        }
        cancelTimer();
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
    public void cancelTimer() {
        if (timer != null) {
            startTimer(timer.startState);
        } else {
            Logger.error("Trying to cancel an invalid timer");
        }
    }

    @Override
    public void onGloryEvent(GloryManager.Status m) {
        Logger.debug("onGloryEvent When Done %s %s", m.name(), state.name());
        switch (state) {
            case CONTINUE_DEPOSIT:
            case IDLE:
                switch (m) {
                    case READY_TO_STORE:
                        state = ActionState.READY_TO_STORE;
                        startTimer(ActionState.READY_TO_STORE);
                        break;
                    case ESCROW_FULL:
                        state = ActionState.ESCROW_FULL;
                        startTimer(ActionState.ESCROW_FULL);
                        break;
                    case COUNTING:
                        cancelTimer();
                        break;
                    case PUT_THE_BILLS_ON_THE_HOPER:
                        startTimer(ActionState.IDLE);
                        break;
                    case IDLE:
                        state = ActionState.FINISH;
                        break;
                    default:
                        Logger.error("onGloryEvent IDLE Invalid manager status %s", m.name());
                        break;
                }
                break;
            case CANCELING:
                switch (m) {
                    case IDLE:
                    case CANCELED:
                        state = ActionState.FINISH;
                        break;
                    case REMOVE_REJECTED_BILLS:
                        break;
                    case REMOVE_THE_BILLS_FROM_ESCROW:
                        break;
                    case REMOVE_THE_BILLS_FROM_HOPER:
                        break;
                    default:
                        Logger.error("onGloryEvent CANCELING Invalid manager status %s", m.name());
                        break;
                }
                break;
            case ESCROW_FULL_STORING:
                if (m != GloryManager.Status.IDLE) {
                    Logger.error("onGloryEvent ESCROW_FULL Invalid manager status %s", m.name());
                }
                state = ActionState.CONTINUE_DEPOSIT;
                break;
            case READY_TO_STORE_STORING:
                if (m != GloryManager.Status.IDLE) {
                    Logger.error("onGloryEventREADY_TO_STORE Invalid manager status %s", m.name());
                }
                state = ActionState.FINISH;
                Deposit d = Deposit.findById(currentDepositId);
                d.finishDate = new Date();
                d.save();
                cancelTimer();
                break;
            case FINISH:
                if (m != GloryManager.Status.IDLE) {
                    Logger.error("onGloryEvent invalid status %s %s", state.name(), m.name());
                }
                break;
            default:
                Logger.error("onGloryEvent invalid status %s %s", state.name(), m.name());
                break;
        }
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.debug("Action ioBoardEvent %s %s", status.status.name(), state.name());
    }

    @Override
    public void onTimeoutEvent(Timeout timeout, ActionState startState) {
        Logger.debug("Action onTimeoutEvent %s", startState.name());
//        if (timeout.timeoutState == TimeoutState.WARN) {
//            timeout.startCancelTimeout();
//        } else {
//            cancel();
//        }
    }
}
