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
import models.db.LgLov;
import models.db.LgUser;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.With;

@With( Secure.class)
public class BillDepositController extends Controller {

    public static void index() {
        Application.index();
    }

    public static void inputReference(String reference1, String reference2) throws Throwable {
        //TODO: Validate references depending on system properties. 
        if (reference1 != null && reference2 != null) {
            LgUser user = Secure.getCurrentUser();
            Integer ref1 = Integer.parseInt(reference1);
            LgLov userCode = DepositUserCodeReference.findByNumericId(ref1);
            if (userCode == null) {
                Logger.error("countMoney: no reference received! for %s", reference1);
                index();
                return;
            }

            Deposit deposit = new Deposit(user, reference2, userCode);
            deposit.save();

            String depositId = deposit.depositId.toString();
            Cache.set(depositId + "-deposit", deposit, "60mn");
            CounterFactory.getGloryManager().count(null);
            countingPage(depositId);
        }
        //depending on a value of LgSystemProperty, show both references or redirect 
        //temporarily until we have a page using getReferences()..
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        render(referenceCodes);
    }

    public static void countingPage(String depositId) {
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);



        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        if (!manager.count(null)) {
            //TODO: Save an event log, do something.
            // if counting is ok, else error ??
        }
        List<Bill> billData = Bill.getCurrentCounters();
        renderArgs.put("billData", billData);
        render(deposit);
    }

    public static void getCountersAndStatus() {
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Status success = manager.getStatus();
        List<Bill> billData = Bill.getCurrentCounters();

        if (request.isAjax()) {
            Object[] o = new Object[2];
            o[0] = success;
            o[1] = billData;
            renderJSON(o);
        } else {
            renderArgs.put("billData", billData);
            render();
        }
    }

    public static void acceptBatch(String depositId) {
        //user accepted to deposit it!
        Logger.info("About to restore data!!!!");

        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);

        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        if (manager.getStatus() != Manager.Status.READY_TO_STORE) {
            Logger.debug("NOT READY TO STORE");
            index();
            return;
        }
        List<Bill> billData = Bill.getCurrentCounters();

        if (!manager.storeDeposit(Integer.parseInt(depositId))) {
            Logger.error("TODO: ERROR DAVE HELP ME");
            index();
            return;
        }

        boolean done = false;
        while (!done) {
            Logger.debug("Current Status %s", manager.getStatus().name());
            switch (manager.getStatus()) {
                case IDLE:
                    done = true;
                    break;
                case ERROR:
                    Logger.error("TODO: ERROR DAVE HELP ME");
                    index();
                    return;
                default:
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                    }
                    break;
            }
        }

        // TODO: if save fails do something interesting
        LgBatch batch = new LgBatch();
        for (Bill bill : billData) {
            LgBill b = new LgBill(batch, bill.quantity, bill.billType, deposit);
            //batch.bills.add(b);
        }
        batch.save();
        flash.success("Deposit is done!");
        render(deposit);
    }

    // TODO: Finish
    public static void cancelDeposit(String depositId) {
        //TODO: Check if there are batches related to this deposit.
        // infrom and send cancelDeposit
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        Manager.ControllerApi manager = CounterFactory.getGloryManager();

        manager.cancelDeposit();
        boolean done = false;
        while (!done) {
            switch (manager.getStatus()) {
                case IDLE:
                    done = true;
                    break;
                case ERROR:
                    Logger.error("TODO: ERROR DAVE HELP ME");
                    index();
                    return;
                default:
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                    }
                    break;
            }
        }
        finishDeposit(depositId);
    }

    public static void finishDeposit(String depositId) {
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        deposit.finishDate = new Date();
        deposit.save();
        Application.index();
    }
}
