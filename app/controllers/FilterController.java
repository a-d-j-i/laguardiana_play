package controllers;

import devices.CounterFactory;
import devices.glory.manager.Manager;
import devices.glory.manager.Manager.Status;
import java.util.Date;
import java.util.List;
import models.Bill;
import models.Deposit;
import models.db.LgBatch;
import models.db.LgBill;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.mvc.With;

@With(Secure.class)
public class FilterController extends BaseController {

    public static void index() {
        Application.index();
    }

    public static void chooseCurrency(Integer currency)
            throws Throwable {
        Currency c = validateCurrency(currency);

        if (currency != null) {
            countingPage(currency);
        }
        //depending on a value of LgSystemProperty, show both references or redirect 
        //temporarily until we have a page using getReferences()..
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        List<Currency> currencies = Currency.findAll();
        render(referenceCodes, currencies);
    }

    public static void countingPage(Integer currency) {
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Currency c = validateCurrency(manager.getCurrency());
        if (c == null) {
            error("Invalid currency");
            return;
        }
        if (request.isAjax()) {
            Status status = manager.getStatus();
            List<Bill> billData = Bill.getCurrentCounters(c.numericId);

            Object[] o = new Object[2];
            o[0] = status;
            o[1] = billData;
            renderJSON(o);
            return;
        }

        // Start counting.
        if (!manager.count(null, c.numericId)) {
            localError("inputReference: error starting the glory %s", manager.getErrorDetail());
            throw new NumberFormatException();
        }

        List<Bill> billData = Bill.getCurrentCounters(c.numericId);
        renderArgs.put("clientCode", getProperty("client_code"));
        renderArgs.put("billData", billData);
        renderArgs.put("currency", c.textId);
        render();
    }

    public static void getCountersAndStatus() {
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Status success = manager.getStatus();
        //List<Bill> billData = Bill.getCurrentCounters();

        if (request.isAjax()) {
            Object[] o = new Object[2];
            o[0] = success;
            //o[1] = billData;
            renderJSON(o);
        } else {
            //renderArgs.put("billData", billData);
            render();
        }
    }

    public static void finishCount(String depositId) {
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        manager.cancelDeposit();
    }
}
