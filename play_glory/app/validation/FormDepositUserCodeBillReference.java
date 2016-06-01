/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import models.Configuration;
import models.db.LgLov;
import models.lov.DepositUserCodeReference;
import play.data.validation.Check;

/**
 *
 * @author adji
 */
public class FormDepositUserCodeBillReference {

    static public class Validate extends Check {

        @Override
        public boolean isSatisfied(Object validatedObject, Object data) {
            return isSatisfied(validatedObject, (FormDepositUserCodeBillReference) data);
        }

        public boolean isSatisfied(Object validatedObject, FormDepositUserCodeBillReference data) {
            try {
                if (!Configuration.mustShowBillDepositReference1()) {
                    return true;
                }
                if (data == null || data.value == null || data.value.isEmpty()) {
                    setMessage("validation.required.reference1");
                    return false;
                }
                DepositUserCodeReference d = DepositUserCodeReference.findByNumericId(Integer.parseInt(data.value));
                if (d == null) {
                    setMessage("validation.required.reference1_invalid_code", data.value.toString());
                    return false;
                }
                data.lov = d;
                return true;
            } catch (NumberFormatException e) {
                setMessage("validation.required.reference1_invalid_code", data.value.toString());
                return false;
            }
        }
    }
    transient public LgLov lov = null;
    public String value = null;

    @Override
    public String toString() {
        return "FormLov{" + "lov=" + lov + ", value=" + value + '}';
    }
}
