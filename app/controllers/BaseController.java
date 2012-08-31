package controllers;

import models.db.LgSystemProperty;
import models.lov.Currency;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

/**
 *
 * @author adji
 *
 * Base controller for all lg controllers, uses security and auth.
 */
@With(Secure.class)
public class BaseController extends Controller {

    @Util
    static public String getProperty(String name) {
        return LgSystemProperty.getProperty(name);
    }

    @Util
    static public Boolean isProperty(String name) {
        return LgSystemProperty.isProperty(name);
    }

    @Util
    public static Boolean localError(String message, Object... args) {
        Logger.error(message, args);
        flash.error(message, args);
        return null;
    }

    @Util
    public static Currency validateCurrency(Integer currency) {
        // Validate Currency.
        if (currency == null) {
            try {
                String dc = getProperty("bill_deposit.default_currency");
                currency = Integer.parseInt(dc);
            } catch (NumberFormatException e) {
                currency = 1; // Fixed.
            }
        }
        Currency c = Currency.findByNumericId(currency);
        if (c == null) {
            localError("inputReference: invalid currency %d", currency);
            return null;
        }
        return c;
    }

    @Util
    public static Boolean validateReference(Boolean r1, Boolean r2, String reference1, String reference2) {
        if (r1) {
            if (reference1 == null) {
                return false;
            }
            if (reference1.isEmpty()) {
                localError("inputReference: reference 1 must not be empty");
                return false;
            }
        }
        if (r2) {
            if (reference2 == null) {
                return false;
            }
            if (reference2.isEmpty()) {
                localError("inputReference: reference 2 must not be empty");
                return false;
            }
        }
        return true;
    }
}