/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.GloryManager;
import java.util.EnumMap;
import models.actions.states.IdleFiltering;
import models.lov.Currency;

/**
 *
 * @author adji
 */
public class FilteringAction extends UserAction {

    static final EnumMap<GloryManager.State, String> messageMap = new EnumMap<GloryManager.State, String>(GloryManager.State.class);

    static {
        messageMap.put(GloryManager.State.IDLE, "counting_page.put_the_bills_on_the_hoper");
        messageMap.put(GloryManager.State.READY_TO_STORE, "counting.ready_to_store");
        messageMap.put(GloryManager.State.PUT_THE_BILLS_ON_THE_HOPER, "counting_page.put_the_bills_on_the_hoper");
        messageMap.put(GloryManager.State.ESCROW_FULL, "counting.escrow_full");
        messageMap.put(GloryManager.State.REMOVE_THE_BILLS_FROM_ESCROW, "counting_page.remove_the_bills_from_escrow");
        messageMap.put(GloryManager.State.REMOVE_REJECTED_BILLS, "counting_page.remove_rejected_bills");
        messageMap.put(GloryManager.State.REMOVE_THE_BILLS_FROM_HOPER, "counting_page.remove_the_bills_from_hoper");
        messageMap.put(GloryManager.State.ERROR, "application.error");
    }

    public FilteringAction(Currency currency, Object formData) {
        super(currency, formData, messageMap);
        state = new IdleFiltering(new StateApi());
    }

    @Override
    final public String getNeededController() {
        return "CountController";
    }

    @Override
    public void start() {
        userActionApi.count(currency.numericId);
    }

    @Override
    public void finish() {
    }
}
