package controllers;

import models.Deposit;
import models.ModelFacade;
import models.TemplatePrinter;
import models.db.LgDeposit;
import models.db.LgSystemProperty;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Util;
import play.mvc.With;

@With(Secure.class)
public class Application extends Controller {

    @Before
    static void wizardFixPage() throws Throwable {
        if (request.isAjax()) {
            return;
        }
        switch (modelFacade.getCurrentStep()) {
            case COUNT_FINISH:
                if (!request.action.equalsIgnoreCase("CountController.finishCount")) {
                    Logger.info("Redirect to CountController.finishCount");
                    redirect(Router.getFullUrl("CountController.finishCount"));
                }
                break;
            case COUNT:
                if (!request.action.equalsIgnoreCase("CountController.countingPage")) {
                    Logger.info("Redirect to CountController.countingPage");
                    redirect(Router.getFullUrl("CountController.countingPage"));
                }
                break;
            case BILL_DEPOSIT:
                if (!request.action.equalsIgnoreCase("BillDepositController.countingPage")) {
                    Logger.info("Redirect to BillDepositController.countingPage");
                    redirect(Router.getFullUrl("BillDepositController.countingPage"));
                }
                break;
            case BILL_DEPOSIT_FINISH:
                if (!request.action.equalsIgnoreCase("BillDepositController.finishDeposit")) {
                    Logger.info("Redirect to BillDepositController.finishDeposit");
                    redirect(Router.getFullUrl("BillDepositController.finishDeposit"));
                }
                break;
            case NONE:
            case RESERVED:
            default: // do nothing
                break;
        }
    }

    public static void index() {
        mainMenu();
    }

    public static void mainMenu() {
        render();
    }

    public static void otherMenu() {
        render();
    }
    static ModelFacade modelFacade = ModelFacade.get();

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
    public static DepositUserCodeReference validateReference1(Boolean r1, String reference1) {
        if (r1) {
            if (reference1 == null) {
                return null;
            }
            if (reference1.isEmpty()) {
                localError("inputReference: reference 1 must not be empty");
                return null;
            }
        }
        try {
            return DepositUserCodeReference.findByNumericId(Integer.parseInt(reference1));
        } catch (NumberFormatException e) {
            localError("inputReference: invalid number for reference %s", reference1);
        }
        return null;
    }

    @Util
    public static String validateReference2(Boolean r2, String reference2) {
        if (r2) {
            if (reference2 == null) {
                return null;
            }
            if (reference2.isEmpty()) {
                localError("inputReference: reference 2 must not be empty");
                return null;
            }
        }
        return reference2;
    }
}
