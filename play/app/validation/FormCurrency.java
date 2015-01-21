package validation;

import models.Configuration;
import models.lov.Currency;
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
                data.value = Configuration.getDefaultCurrency();
            }
            data.currency = Currency.findByNumericId(data.value);
            if (data.currency == null) {
                setMessage("validation.required.currency_invalid_code", data.value.toString());
                return false;
            }
            return true;
        }
    }

    public FormCurrency(Integer value) {
        this.value = value;
    }

    public FormCurrency() {
    }
    transient public Currency currency = null;
    public Integer value = null;

    @Override
    public String toString() {
        return "FormCurrency{" + ", value=" + value + '}';
    }
}
