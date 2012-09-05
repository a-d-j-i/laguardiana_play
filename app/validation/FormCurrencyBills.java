/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.util.List;
import models.db.LgBillType;

/**
 *
 * @author adji
 */
public class FormCurrencyBills extends FormCurrency {

    static public class Validate extends FormCurrency.Validate {

        @Override
        public boolean isSatisfied(Object validatedObject, Object data) {
            return isSatisfied(validatedObject, (FormCurrencyBills) data);
        }

        public boolean isSatisfied(Object validatedObject, FormCurrencyBills data) {
            if (!super.isSatisfied(validatedObject, validatedObject)) {
                return false;
            }
            // TODO: Finish.
            return true;
        }
    }
    transient public List<LgBillType> bills = null;
    public List<Integer> billValues = null;

    @Override
    public String toString() {
        return "FormCurrencyBills{" + "bills=" + bills + ", billValues=" + billValues + '}';
    }
}
