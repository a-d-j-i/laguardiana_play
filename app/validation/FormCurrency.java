/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import models.db.LgSystemProperty;
import models.lov.Currency;
import play.Logger;
import play.data.validation.Check;

/**
 *
 * @author adji
 */
public class FormCurrency {

    static public class Validate extends Check {

        @Override
        public boolean isSatisfied(Object validatedObject, Object data) {
            return isSatisfied(validatedObject, (FormCurrency) data);
        }

        public boolean isSatisfied(Object validatedObject, FormCurrency data) {
            // Validate Currency.
            if (data == null) {
                // Accept null properites, this isn't a required property.
                return true;
            }
            if (data.value == null) {
                try {
                    String dc = LgSystemProperty.getProperty(LgSystemProperty.Types.DEFAULT_CURRENCY);
                    data.value = Integer.parseInt(dc);
                } catch (NumberFormatException e) {
                    data.value = 1; // Fixed.
                }
            }
            data.currency = Currency.findByNumericId(data.value);
            if (data.currency == null) {
                setMessage("validation.required.currency_invalid_code", data.value.toString());
                return false;
            }
            return true;
        }
    }
    transient public Currency currency = null;
    public Integer value = null;

    @Override
    public String toString() {
        return "FormCurrency{" + "currency=" + currency + ", value=" + value + '}';
    }
}
