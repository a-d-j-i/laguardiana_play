/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import models.actions.states.IdleFiltering;
import models.lov.Currency;

/**
 *
 * @author adji
 */
public class FilteringAction extends UserAction {

    public FilteringAction(Currency currency, Object formData) {
        super(currency, formData);
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
