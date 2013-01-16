/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.ManagerInterface;
import java.util.EnumMap;
import models.actions.states.IdleCounting;
import models.lov.Currency;

/**
 *
 * @author adji
 */
public class CountingAction extends UserAction {

    static final EnumMap<ManagerInterface.ManagerState, String> messageMap = new EnumMap<ManagerInterface.ManagerState, String>(ManagerInterface.ManagerState.class);

    static {
        messageMap.put(ManagerInterface.ManagerState.READY_TO_STORE, "counting.ready_to_store");
        messageMap.put(ManagerInterface.ManagerState.ESCROW_FULL, "counting.escrow_full");
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
