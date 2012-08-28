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
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.mvc.With;

@With(Secure.class)
public class BillDepositController extends BaseController {

    public static void index() {
        Application.index();
    }

    public static void inputReference(String reference1, String reference2) 
        throws Throwable {
        //TODO: Validate references depending on system properties. 
        Deposit d = DepositController.createDeposit(reference1, reference2);
        if (d != null) {
            countingPage(d.depositId.toString());
            return;
        }
        //depending on a value of LgSystemProperty, show both references or redirect 
        //temporarily until we have a page using getReferences()..
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        render(referenceCodes);
    }

    public static void countingPage(String depositId) {
        Integer currency = 1;
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        Manager.ControllerApi manager = CounterFactory.getGloryManager();

        
        Boolean countStartOk = manager.count(null, currency);
        
        if (request.isAjax()) {
            Object[] o = new Object[1];
            o[0] = countStartOk;
            renderJSON(o);
        } else {
            if (!countStartOk) {
                //TODO: Save an event log, do something.
                // if counting is ok, else error ??
            }
            List<Bill> billData = Bill.getCurrentCounters();
            renderArgs.put("billData", billData);
            render(deposit);
        };
    }

    public static void continueDeposit(String depositId) {
        Integer currency = 1;
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);

        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        if (!manager.count(null, currency)) {
            //TODO: Save an event log, do something.
            // if counting is ok, else error ??
        }
        List<Bill> billData = Bill.getCurrentCounters();
        renderArgs.put("billData", billData);
        render(deposit);
    }
    
    public static void getCountersAndStatus() {
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Status status = manager.getStatus();
        List<Bill> billData = Bill.getCurrentCounters();

        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = status;
            o[1] = billData;
            o[2] = (status == Manager.Status.READY_TO_STORE) || (status == Manager.Status.ESCROW_FULL);
            renderJSON(o);
        } else {
            renderArgs.put("billData", billData);
            render();
        }
    }

    public static void acceptBatch(String depositId) {
        //user accepted to deposit it!
        Object[] o = new Object[2];
        Boolean storeOk;

        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Manager.Status status = manager.getStatus();
        Logger.info("About to restore data!!!!");

        if ((status != Manager.Status.READY_TO_STORE) && 
                (status != Manager.Status.ESCROW_FULL)) {
            Logger.debug("NOT READY TO STORE");
            index();
            return;
        }

        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        List<Bill> billData = Bill.getCurrentCounters();

        
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
            LgBill b = new LgBill(batch, bill.quantity, bill.billType, deposit);
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
        Boolean statusOk = (status == Manager.Status.IDLE || 
                        status == Manager.Status.ESCROW_FULL ||
                        status == Manager.Status.REMOVE_THE_BILLS_FROM_HOPER);
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

    // TODO: Finish
    public static void cancelDeposit(String depositId) {
        //TODO: Check if there are batches related to this deposit.
        // infrom and send cancelDeposit
        //Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Logger.error(" / Cancel deposit");

        manager.cancelDeposit();
        Object[] o = new Object[1];
        o[0] = 1;
        Logger.error(" // Cancel deposit");
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
