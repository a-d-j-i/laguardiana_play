package controllers;

import java.util.List;
import models.ModelFacade;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.mvc.Before;

public class FilterController extends Application {

    @Before
    static void wizardFixPage() throws Throwable {
        switch (modelFacade.getCurrentStep()) {
            case COUNT:
            case COUNT_FINISH:
                break;
            case NONE:
                if (request.actionMethod.equalsIgnoreCase("chooseCurrency")) {
                    break;
                }
            case BILL_DEPOSIT:
            case BILL_DEPOSIT_FINISH:
            case RESERVED:
            default: // do nothing
                Application.index();
                break;
        }
    }

    public static void chooseCurrency(Integer currency)
            throws Throwable {
        Currency c = validateCurrency(currency);

        // TODO: Use form validation.
        if (c != null) {
            modelFacade.startCounting(c);
            countingPage();
            return;
        }
        //depending on a value of LgSystemProperty, show both references or redirect 
        //temporarily until we have a page using getReferences()..
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        List<Currency> currencies = Currency.findAll();
        render(referenceCodes, currencies);
    }

    public static void countingPage() {
        ModelFacade.BillDepositStartData data = modelFacade.getStartBillDepositData();
        if (request.isAjax()) {
            Object[] o = new Object[2];
            o[0] = data.getStatus();
            o[1] = data.getBillData();
            renderJSON(o);
        } else {
            renderArgs.put("clientCode", getProperty("client_code"));
            render(data);
        }
    }

    public static void cancelCount() {
        modelFacade.cancelDeposit();
        countingPage();
    }

    public static void finishCount() {
        modelFacade.finishDeposit();
        Application.index();
    }
}
