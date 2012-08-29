package controllers;

import models.db.LgSystemProperty;
import models.lov.Currency;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

/**
 *
 * @author adji
 *
 * Base controller for all lg controllers, uses security and auth.
 */
@With(Secure.class)
public class BaseController extends Controller {

    static public String getProperty(String name) {
        return LgSystemProperty.getProperty(name);
    }

    static public Boolean isProperty(String name) {
        return LgSystemProperty.isProperty(name);
    }

    public static Boolean localError(String message, Object... args) {
        Logger.error(message, args);
        flash.error(message, args);
        return null;
    }

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
}