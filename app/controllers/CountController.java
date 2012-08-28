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
import models.lov.MoneyUnit;
import play.Logger;
import play.mvc.With;

@With(Secure.class)
public class CountController extends BaseController {

    public static void index() {
        Application.index();
    }

    public static void chooseCurrency(String currency) throws Throwable {
        //TODO: Create event.       
        List<MoneyUnit> moneyUnits = MoneyUnit.findAll();
        render(moneyUnits);
    }

    public static void countingPage(String depositId) {
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
        Object[] o = new Object[1];
        Boolean storeOk;
        Logger.info("About to restore data!!!!");

        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        if (manager.getStatus() != Manager.Status.READY_TO_STORE) {
            Logger.debug("NOT READY TO STORE");
            index();
            return;
        }

        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);
        List<Bill> billData = Bill.getCurrentCounters();

        o[0] = storeOk = manager.storeDeposit(Integer.parseInt(depositId));
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
        Boolean finished = (status == Manager.Status.IDLE
                || status == Manager.Status.ERROR);
        o[0] = finished;
        o[1] = status == Manager.Status.IDLE;

        Logger.error("-----------");
        Logger.error(" finished: %b result: %b", finished, (status == Manager.Status.IDLE));
        if (!finished) {
            renderJSON(o);
            return;
        }
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
