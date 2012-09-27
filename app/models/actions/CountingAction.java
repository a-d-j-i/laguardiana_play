/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.GloryManager;
import devices.io_board.IoBoard;
import java.util.EnumMap;
import java.util.List;
import models.lov.Currency;
import play.Logger;
import validation.Bill;

/**
 *
 * @author adji
 */
public class CountingAction extends UserAction {

    static final EnumMap<GloryManager.Status, String> messageMap = new EnumMap<GloryManager.Status, String>(GloryManager.Status.class);

    static {
        messageMap.put(GloryManager.Status.IDLE, "counting_page.put_the_bills_on_the_hoper");
        messageMap.put(GloryManager.Status.READY_TO_STORE, "counting.ready_to_store");
        messageMap.put(GloryManager.Status.PUT_THE_BILLS_ON_THE_HOPER, "counting_page.put_the_bills_on_the_hoper");
        messageMap.put(GloryManager.Status.ESCROW_FULL, "counting.escrow_full");
        messageMap.put(GloryManager.Status.REMOVE_THE_BILLS_FROM_ESCROW, "counting_page.remove_the_bills_from_escrow");
        messageMap.put(GloryManager.Status.REMOVE_REJECTED_BILLS, "counting_page.remove_rejected_bills");
        messageMap.put(GloryManager.Status.REMOVE_THE_BILLS_FROM_HOPER, "counting_page.remove_the_bills_from_hoper");
        messageMap.put(GloryManager.Status.ERROR, "application.error");
    }
    public Currency currency;

    public CountingAction(Currency currency, Object formData) {
        super(formData, messageMap);
        this.currency = currency;
    }

    @Override
    final public String getNeededController() {
        return "CountController";
    }

    @Override
    public void start() {
        if (!userActionApi.count(currency.numericId)) {
            state = ActionState.ERROR;
            error = String.format("startCounting can't start glory %s", userActionApi.getErrorDetail());
        }
    }

    public List<Bill> getBillData() {
        return Bill.getCurrentCounters(currency.numericId);
    }

    public void acceptDeposit() {
        if (state != ActionState.READY_TO_STORE) {
            Logger.error("acceptDeposit Invalid step");
            return;
        }
        if (!userActionApi.cancelDeposit()) {
            Logger.error("startCounting can't cancel glory");
        }
    }

    public void cancelDeposit() {
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
    public void gloryDone(GloryManager.Status m, GloryManager.ErrorDetail me) {
        Logger.debug("CountingAction When Done %s %s", m.name(), state.name());
        switch (state) {
            case IDLE:
                switch (userActionApi.getManagerStatus()) {
                    case ERROR:
                        state = ActionState.ERROR;
                        break;
                    case READY_TO_STORE:
                        state = ActionState.READY_TO_STORE;
                        break;
                    case ESCROW_FULL:
                        if (!userActionApi.withdrawDeposit()) {
                            error("startCounting can't withdrawDeposit glory");
                        }
                        break;
                    default:
                        Logger.debug("getControllerAction Current manager state %s %s",
                                state.name(), userActionApi.getManagerStatus().name());
                        break;
                }
                return;
            case CANCELING:
                // IDLE is when we canceled after an full escrow
                if (m != GloryManager.Status.CANCELED) {
                    error("CANCELING Invalid manager status %s", m.name());
                } else {
                    state = ActionState.FINISH;
                }
                break;
            case READY_TO_STORE:
                if (m != GloryManager.Status.CANCELED) {
                    Logger.error("READY_TO_STORE Invalid manager status %s", m.name());
                }
                state = ActionState.FINISH;
                break;
            default:
                error("WhenDone invalid status %s %s %s", state.name(), m.name(), me);
                return;
        }
        if (state == ActionState.ERROR) {
            return;
        }

        Logger.debug("--------- esNewClasscrow full SAVE");
    }

    @Override
    public void ioBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.debug("CountingAction ioBoardEvent %s %s", status.status.name(), state.name());
    }
}
