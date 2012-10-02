/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.IoBoard;
import devices.glory.manager.GloryManager;
import java.util.EnumMap;
import models.lov.Currency;
import play.Logger;

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

    public CountingAction(Currency currency, Object formData, int timeout) {
        super(currency, formData, messageMap, timeout);
    }

    @Override
    final public String getActionNeededController() {
        return "CountController";
    }

    @Override
    public void start() {
        userActionApi.count(currency.numericId);
    }

    @Override
    public void cancel() {
        state = ActionState.CANCELING;
        if (!userActionApi.cancelDeposit()) {
            Logger.error("cancelDeposit can't cancel glory");
        }
    }

    @Override
    public void accept() {
        if (state != ActionState.READY_TO_STORE) {
            Logger.error("acceptDeposit Invalid step");
            return;
        }
        if (!userActionApi.cancelDeposit()) {
            Logger.error("startCounting can't cancel glory");
        }
    }

    @Override
    public void onGloryEvent(GloryManager.Status m) {
        Logger.debug("CountingAction When Done %s %s", m.name(), state.name());
        switch (state) {
            case IDLE:
                switch (m) {
                    case READY_TO_STORE:
                        state = ActionState.READY_TO_STORE;
                        break;
                    case ESCROW_FULL:
                        userActionApi.withdraw();
                        break;
                    default:
                        Logger.debug("getControllerAction Current manager state %s %s", state.name(), m.name());
                        break;
                }
                return;
            case CANCELING:
                // IDLE is when we canceled after an full escrow
                if (m != GloryManager.Status.CANCELED) {
                    Logger.error("CANCELING Invalid manager status %s", m.name());
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
                Logger.error("WhenDone invalid status %s %s %s", state.name(), m.name());
                return;
        }
        Logger.debug("--------- esNewClasscrow full SAVE");
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
