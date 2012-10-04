/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.GloryManager;
import java.util.EnumMap;
import models.actions.states.IdleCounting;
import models.lov.Currency;

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

    public CountingAction(Currency currency, Object formData) {
        super(currency, formData, messageMap);
        state = new IdleCounting(new StateApi());
    }

    @Override
    final public String getActionNeededController() {
        return "CountController";
    }

    @Override
    public void start() {
        userActionApi.count(currency.numericId);
    }
}
