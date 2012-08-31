package controllers;

import java.util.List;
import models.Deposit;
import models.ModelFacade;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.mvc.Before;

public class BillDepositController extends Application {

    @Before
    static void wizardFixPage() throws Throwable {
        switch (modelFacade.getCurrentStep()) {
            case BILL_DEPOSIT:
            case BILL_DEPOSIT_FINISH:
                break;
            case NONE:
                if (request.actionMethod.equalsIgnoreCase("inputReference")) {
                    break;
                }
            case RESERVED:
            default: // do nothing
                Application.index();
                break;
        }
    }

    public static void inputReference(String reference1, String reference2, Integer currency) throws Throwable {
        Boolean r1 = isProperty("bill_deposit.show_reference1");
        Boolean r2 = isProperty("bill_deposit.show_reference2");

        // Validate Currency.
        Currency c = validateCurrency(currency);
        if (c == null) {
            localError("inputReference: invalid currency %d", currency);
        }
        DepositUserCodeReference userCodeLov = validateReference1(r1, reference1);
        String userCode = validateReference2(r2, reference2);

        // TODO: Use form validation.
        if (c != null && userCodeLov != null && userCode != null) {
            modelFacade.startBillDeposit(userCodeLov, userCode, c);
            countingPage();
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

    public static void cancelDeposit() {
        modelFacade.cancelBillDeposit();
        countingPage();
    }

    public static void acceptBatch() {
        modelFacade.acceptBillDeposit();
        countingPage();
    }

    public static void finishDeposit() {
        Deposit data = modelFacade.getDeposit();
        modelFacade.finishDeposit();
        if (data == null) {
            Application.index();
            return;
        }
        renderArgs.put("clientCode", getProperty("client_code"));
        renderArgs.put("userCode", data.userCode);
        if ( data.userCodeData != null ) {
            renderArgs.put("userCodeLov", data.userCodeData.description);
        }
        renderArgs.put("currency", data.currencyData.textId);
        renderArgs.put("depositTotal", data.getTotal());
        render();
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
