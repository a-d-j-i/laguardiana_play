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
import models.manager.ModMan;
import play.Logger;
import play.mvc.With;

@With(Secure.class)
public class BillDepositController extends BaseController {

    public static void index() {
        ModMan modman = ModMan.get();
        Application.index();
    }

    public static void inputReference(String reference1, String reference2, Integer currency)
            throws Throwable {
        ModMan modman = ModMan.get();

        if (modman.currentOperation() == ModMan.Operations.IDLE) {
            if (!modman.CreateCashDeposit()) {
                Application.index();
            }
        }


        Boolean r1 = isProperty("bill_deposit.show_reference1");
        Boolean r2 = isProperty("bill_deposit.show_reference2");

        updateDeposit(r1, r2, reference1, reference2, currency);
        Boolean ready = modman.getCashDeposit().readyFor(
                ModMan.CashDeposit.Screens.CASH_DEPOSIT_COUNT);
        Logger.info("ready? %b", ready);
        if (ready) {
            countingPage("145");
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
        ModMan modman = ModMan.get();
        modman.getCashDeposit().switchTo(ModMan.CashDeposit.Screens.CASH_DEPOSIT_COUNT);
        Deposit deposit = modman.getCashDeposit().deposit;

        List<Bill> billData = Bill.getCurrentCounters(deposit.currency);
        renderArgs.put("clientCode", getProperty("client_code"));
        renderArgs.put("depositId", deposit.depositId);
        renderArgs.put("userCode", deposit.userCode);
        renderArgs.put("userCodeLov", DepositUserCodeReference.findByNumericId(
                deposit.userCodeLov).description);
        renderArgs.put("billData", billData);
        renderArgs.put("currency", c.textId);
        render(deposit);
    }

    public static void summary(String depositId) {
        List<Bill> billData = Bill.getCurrentCounters(1);//deposit.currency);
        renderArgs.put("clientCode", getProperty("client_code"));
        renderArgs.put("userCode", "USERCODE" );//deposit.userCode);
        renderArgs.put("userCodeLov", "USERCODELOV" ); //DepositUserCodeReference.findByNumericId(deposit.userCodeLov).description);
        renderArgs.put("billData", billData);
        renderArgs.put("currency", "CURRENCY" );//c.textId);
        renderArgs.put("depositTotal", "DEPOSITTOTAL" );//c.textId);
        
        render();
    }

    public static Integer updateDeposit(Boolean r1, Boolean r2,
            String reference1, String reference2, Integer currency) { //throws Throwable 
  
        if (!validateReference(r1, r2, reference1, reference2)) {
            return null;
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

        ModMan modman = ModMan.get();
        modman.getCashDeposit().deposit.userCode = reference2;
        modman.getCashDeposit().deposit.userCodeLov = userCode.numericId;
        Logger.info("currency setuped: %d", currency);
        modman.getCashDeposit().deposit.currency = currency;
        return null;
    }

    public static void info() {
        if (!request.isAjax()) {
            Application.index();
            return;
        }
        ModMan modman = ModMan.get();

        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Currency c = validateCurrency(manager.getCurrency());
        Status status = manager.getStatus();
        List<Bill> billData = Bill.getCurrentCounters(c.numericId);

        Object[] o = new Object[2];
        o[0] = status;
        o[1] = billData;
        renderJSON(o);
        return;
    }

    public static void cancelDeposit(String depositId) {
        ModMan modman = ModMan.get();
        Logger.info("in cancel batch");
        Deposit deposit = modman.getCashDeposit().deposit;
        modman.getCashDeposit().switchTo(ModMan.CashDeposit.Screens.CASH_DEPOSIT_CANCEL);
        String clientCode = getProperty("client_code");
        render(deposit, clientCode);
    }

//    ///////////////////////////////
//    public static void continueDeposit(String depositId) {
//        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
//        if (deposit == null) {
//            error("continueDeposit: invalid deposit");
//        }
//        Manager.ControllerApi manager = CounterFactory.getGloryManager();
//        Currency c = validateCurrency(manager.getCurrency());
//        if (c == null) {
//            error("Invalid currency");
//            return;
//        }
//
//        List<Bill> billData = Bill.getCurrentCounters(deposit.currency);
//
//        renderArgs.put("billData", billData);
//        render(deposit);
//    }
    public static void acceptBatch(String depositId) {
        ModMan modman = ModMan.get();
        modman.getCashDeposit().switchTo(ModMan.CashDeposit.Screens.CASH_DEPOSIT_ACCEPT);
        Application.index();
    }
//    public static void checkAcceptBatch(String depositId) {
//        Manager.ControllerApi manager = CounterFactory.getGloryManager();
//        Object[] o = new Object[3];
//        Manager.Status status = manager.getStatus();
//        Boolean statusOk = (status == Manager.Status.IDLE
//                || status == Manager.Status.ESCROW_FULL
//                || status == Manager.Status.REMOVE_THE_BILLS_FROM_HOPER);
//        Boolean finished = (statusOk || status == Manager.Status.ERROR);
//        o[0] = finished;
//        o[1] = statusOk;
//
//        Logger.error(" finished: %b result: %b", finished, statusOk);
//        //if (!finished) {
//        //    renderJSON(o);
//        //    return;
//        //}
//        renderJSON(o);
//    }
//
//    public static void checkCancelDeposit(String depositId) {
//        Manager.ControllerApi manager = CounterFactory.getGloryManager();
//        Object[] o = new Object[3];
//        Manager.Status status = manager.getStatus();
//        Boolean finished = (status == Manager.Status.IDLE
//                || status == Manager.Status.ERROR);
//        o[0] = finished;
//        o[1] = status == Manager.Status.IDLE;
//
//        Logger.error(" finished: %b result: %b", finished, (status == Manager.Status.IDLE));
//
//        if (finished) {
//            //Logger.error("pre finish deposit");
//            //finishDeposit(depositId);
//            //Logger.error("after finish deposit");
//            Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
//            deposit.finishDate = new Date();
//            deposit.save();
//        }
//        Logger.error("about to render json..");
//        renderJSON(o);
//    }
//
//    public static void finishDeposit(String depositId) {
//        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
//        deposit.finishDate = new Date();
//        deposit.save();
//    }
}
