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

    static final EnumMap<GloryManager.State, String> messageMap = new EnumMap<GloryManager.State, String>(GloryManager.State.class);

    static {
        messageMap.put(GloryManager.State.READY_TO_STORE, "counting.ready_to_store");
        messageMap.put(GloryManager.State.ESCROW_FULL, "counting.escrow_full");
    }

    public CountingAction(Currency currency, Object formData) {
        super(currency, formData, messageMap);
        state = new IdleCounting(new StateApi());
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
