package controllers;

import models.Deposit;
import models.TemplatePrinter;
import models.db.LgSystemProperty;
import models.lov.Currency;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

@With(Secure.class)
public class Application extends Controller {

    public static void index() {
        mainMenu();
    }

    public static void mainMenu() {
        render();
    }

    public static void otherMenu() {
        render();
    }

    public static void printTemplate() {
        TemplatePrinter.printTemplate("<h1>My First Heading</h1><p>My first paragraph.</p>");
        redirect("Application.index");
    }

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
        Currency c = Deposit.validateCurrency(currency);
        if (c == null) {
            localError("validateCurrency: invalid currency %d", currency);
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
