package controllers;

import devices.CounterFactory;
import devices.glory.manager.Manager;
import devices.glory.manager.Manager.Status;
import java.util.Date;
import java.util.List;
import models.Bill;
import models.Deposit;
import models.db.*;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.mvc.With;

@With(Secure.class)
public class BillDepositController extends BaseController {

    public static void index() {
        Application.index();
    }

    public static Integer validateAndCreateDeposit(Boolean r1, Boolean r2, String reference1, String reference2, Integer currency) throws Throwable {
        // empty 
        if (r1) {
            if (reference1 == null) {
                return null;
            }
            if (reference1.isEmpty()) {
                localError("inputReference: reference 1 must not be empty");
                return null;
            }
        }
        if (r2) {
            if (reference2 == null) {
                return null;
            }
            if (reference2.isEmpty()) {
                localError("inputReference: reference 2 must not be empty");
                return null;
            }
        }

        // Validate Currency.
        Currency c = validateCurrency(currency);
        if (c == null) {
            localError("inputReference: invalid currency %d", currency);
            return null;
        }

        LgLov userCode;
        try {
            userCode = DepositUserCodeReference.findByNumericId(Integer.parseInt(reference1));
        } catch (NumberFormatException e) {
            localError("inputReference: invalid number for reference %s", reference1);
            return null;
        }
        if (userCode == null) {
            localError("inputReference: no reference received! for %s", reference1);
            return null;
        }
        LgUser user = Secure.getCurrentUser();
        Deposit deposit = new Deposit(user, reference2, userCode, c);
        deposit.save();
        return deposit.depositId;
    }

    public static void inputReference(String reference1, String reference2, Integer currency)
            throws Throwable {

        Boolean r1 = isProperty("bill_deposit.show_reference1");
        Boolean r2 = isProperty("bill_deposit.show_reference2");

        Integer depositId = validateAndCreateDeposit(r1, r2, reference1, reference2, currency);
        if (depositId != null) {
            countingPage(depositId.toString());
            return;
        }

        //depending on a value of LgSystemProperty, show both references or redirect 
        //temporarily until we have a page using getReferences()..
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        List<Currency> currencies = Currency.findAll();
        renderArgs.put("showReference1", r1);
        renderArgs.put("showReference2", r2);
        render(referenceCodes, currencies);
    }

    public static void countingPage(String depositId) {

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

        if (depositId == null) {
            localError("countingPage: invalid depositId");
        }
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        if (deposit == null) {
            error("countingPage: invalid deposit");
        }

        List<Bill> billData = Bill.getCurrentCounters(deposit.currency);
        renderArgs.put("clientCode", getProperty("client_code"));
        renderArgs.put("depositId", deposit.depositId);
        renderArgs.put("userCode", deposit.userCode);
        renderArgs.put("userCodeLov", DepositUserCodeReference.findByNumericId(deposit.userCodeLov).description);
        renderArgs.put("billData", billData);
        renderArgs.put("currency", c.textId);
        render(deposit);
    }

    public static void cancelDeposit(String depositId) {
        if (depositId == null) {
            localError("countingPage: invalid depositId");
        }
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        if (deposit == null) {
            error("countingPage: invalid deposit");
        }

        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Logger.error(" / Cancel deposit");

        manager.cancelDeposit();
        render();
    }

    ///////////////////////////////
    public static void continueDeposit(String depositId) {
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        if (deposit == null) {
            error("continueDeposit: invalid deposit");
        }
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Currency c = validateCurrency(manager.getCurrency());
        if (c == null) {
            error("Invalid currency");
            return;
        }

        List<Bill> billData = Bill.getCurrentCounters(deposit.currency);

        renderArgs.put("billData", billData);
        render(deposit);
    }

    public static void acceptBatch(String depositId) {
        //user accepted to deposit it!
        Object[] o = new Object[2];
        Boolean storeOk;

        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Manager.Status status = manager.getStatus();
        Logger.info("About to restore data!!!!");

        if ((status != Manager.Status.READY_TO_STORE)
                && (status != Manager.Status.ESCROW_FULL)) {
            Logger.debug("NOT READY TO STORE");
            index();
            return;
        }

        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        List<Bill> billData = Bill.getCurrentCounters(deposit.currency);


        //temporarily cancel deposit
        o[0] = storeOk = manager.storeDeposit(Integer.parseInt(depositId));
        //o[0] = storeOk = manager.cancelDeposit();
        o[1] = status == Manager.Status.ESCROW_FULL; //shall we continue?

        Logger.error(" // accept deposit opened result: %b", storeOk);
        if (!storeOk) {
            renderJSON(o);
            return;
        }

        LgBatch batch = new LgBatch();
        for (Bill bill : billData) {
            Logger.debug(" -> quantity %d", bill.quantity);
            LgBillType bt = LgBillType.findById(bill.billTypeId);
            LgBill b = new LgBill(batch, bill.quantity, bt, deposit);
            //batch.bills.add(b);
        }
        batch.save();
        renderJSON(o);
        return;
    }

    public static void checkAcceptBatch(String depositId) {
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Object[] o = new Object[3];
        Manager.Status status = manager.getStatus();
        Boolean statusOk = (status == Manager.Status.IDLE
                || status == Manager.Status.ESCROW_FULL
                || status == Manager.Status.REMOVE_THE_BILLS_FROM_HOPER);
        Boolean finished = (statusOk || status == Manager.Status.ERROR);
        o[0] = finished;
        o[1] = statusOk;

        Logger.error(" finished: %b result: %b", finished, statusOk);
        //if (!finished) {
        //    renderJSON(o);
        //    return;
        //}
        renderJSON(o);
    }

    public static void checkCancelDeposit(String depositId) {
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Object[] o = new Object[3];
        Manager.Status status = manager.getStatus();
        Boolean finished = (status == Manager.Status.IDLE
                || status == Manager.Status.ERROR);
        o[0] = finished;
        o[1] = status == Manager.Status.IDLE;

        Logger.error(" finished: %b result: %b", finished, (status == Manager.Status.IDLE));

        if (finished) {
            //Logger.error("pre finish deposit");
            //finishDeposit(depositId);
            //Logger.error("after finish deposit");
            Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
            deposit.finishDate = new Date();
            deposit.save();
        }
        Logger.error("about to render json..");
        renderJSON(o);
    }

    public static void finishDeposit(String depositId) {
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        deposit.finishDate = new Date();
        deposit.save();
    }
}
