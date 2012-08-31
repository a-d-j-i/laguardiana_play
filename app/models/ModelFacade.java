/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controllers.Secure;
import devices.CounterFactory;
import devices.glory.manager.Manager;
import java.util.Date;
import java.util.List;
import models.db.LgBatch;
import models.db.LgBill;
import models.db.LgBillType;
import models.db.LgUser;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;

/**
 *
 * @author aweil
 */
public class ModelFacade {
    // FACTORY

    static private ModelFacade THIS = new ModelFacade();

    static public ModelFacade get() {
        return THIS;
    }

    // Used by the controllers to fix the current page.
    static public enum CurrentStep {

        NONE,
        RESERVED,
        BILL_DEPOSIT,
        BILL_DEPOSIT_FINISH,
        BILL_DEPOSIT_ERROR,
        COUNT,
        COUNT_FINISH,;
    }
    private Manager.ControllerApi manager = CounterFactory.getGloryManager();
    private CurrentStep currentStep = CurrentStep.NONE;
    private User currentUser = null;
    private Deposit currentDeposit = null;

    private void error(String msg) {
        Logger.error("ERROR IN ModelFacade %s", msg);
        throw new play.mvc.results.Error(msg);
    }

    synchronized public CurrentStep getCurrentStep() {
        if (currentUser == null) {
            if (currentStep != CurrentStep.NONE) {
                error(String.format("startDeposit Invalid step %s", currentStep.name()));
            }
            return CurrentStep.NONE;
        }
        if (currentUser.equals(Secure.getCurrentUser())) {
            return currentStep;
        } else {
            return CurrentStep.RESERVED;
        }
    }

    protected class OnCountDone implements Runnable {

        public void run() {
            synchronized (ModelFacade.this) {
                Logger.error("COUNTDONECOUNTDONECOUNTDONECOUNTDONE %s", manager.getStatus().name());
                Manager.Status m = manager.getStatus();
                if (currentStep == CurrentStep.COUNT) {
                    if (m != Manager.Status.CANCELED) {
                        Logger.error("OnCountDone invalid manager status");
                        currentStep = CurrentStep.BILL_DEPOSIT_ERROR;
                    }
                    currentStep = CurrentStep.COUNT_FINISH;
                    return;
                }
                if (currentStep != CurrentStep.BILL_DEPOSIT) {
                    Logger.error("OnCountDone invalid step");
                    currentStep = CurrentStep.BILL_DEPOSIT_ERROR;
                }
                switch (m) {
                    case CANCELED:
                        currentDeposit = null;
                        currentStep = CurrentStep.BILL_DEPOSIT_FINISH;
                        break;

                    case IDLE:
                        //saveDeposit();
                        currentStep = CurrentStep.BILL_DEPOSIT_FINISH;
                        break;
                    case ESCROW_FULL:
                        //saveDeposit();
                        break;
                    case ERROR:
                        Logger.error("OnCountDone invalid machine error %s", manager.getErrorDetail().toString());
                        currentStep = CurrentStep.BILL_DEPOSIT_ERROR;
                        break;
                    default:
                        Logger.error("OnCountDone invalid machine status %s", m);
                        currentStep = CurrentStep.BILL_DEPOSIT_ERROR;
                        break;
                }
            }
        }
    }

    synchronized public void startCounting(Currency c) {
        if (getCurrentStep() != CurrentStep.NONE) {
            error(String.format("startBillDeposit Invalid step %s", currentStep.name()));
            return;
        }
        currentUser = Secure.getCurrentUser();
        if (currentDeposit != null) {
            error("startBillDeposit Invalid deposit");
            return;
        }
        currentDeposit = new Deposit(currentUser, null, null, c);
        if (!manager.count(new OnCountDone(), null, c.numericId)) {
            error("startBillDeposit can't start glory");
            return;
        }
        currentStep = CurrentStep.COUNT;
    }

    synchronized public void startBillDeposit(DepositUserCodeReference userCodeLov, String userCode, Currency c) {
        if (getCurrentStep() != CurrentStep.NONE) {
            error(String.format("startBillDeposit Invalid step %s", currentStep.name()));
            return;
        }
        currentUser = Secure.getCurrentUser();
        if (currentDeposit != null) {
            error("startBillDeposit Invalid deposit");
            return;
        }
        currentDeposit = new Deposit(currentUser, userCode, userCodeLov, c);
        if (!manager.count(new OnCountDone(), null, c.numericId)) {
            error("startBillDeposit can't start glory");
            return;
        }
        currentStep = CurrentStep.BILL_DEPOSIT;
    }

    synchronized public void acceptBillDeposit() {
        if (getCurrentStep() != CurrentStep.BILL_DEPOSIT || currentDeposit == null) {
            error("cancelBillDeposit Invalid step");
            return;
        }

        currentDeposit.save();
        // TODO: Commit transaction, check errors
        if (!manager.storeDeposit(currentDeposit.depositId)) {
            error("startBillDeposit can't cancel glory");
        }
    }

    synchronized public void cancelBillDeposit() {
        if (currentDeposit == null
                || (getCurrentStep() != CurrentStep.BILL_DEPOSIT && getCurrentStep() != CurrentStep.COUNT)) {
            error("cancelBillDeposit Invalid step");
            return;
        }
        if (!manager.cancelDeposit()) {
            error("startBillDeposit can't cancel glory");
        }
    }

    synchronized public void finishDeposit() {
        if (getCurrentStep() != CurrentStep.BILL_DEPOSIT_FINISH) {
            error("finishDeposit Invalid step");
            return;
        }
        currentUser = null;
        currentStep = CurrentStep.NONE;
        currentDeposit = null;
    }

    // Protect private items, todo check current status.
    public class BillDepositStartData {

        public String getUserCode() {
            synchronized (ModelFacade.this) {
                if (currentDeposit != null) {
                    return currentDeposit.userCode;
                }
                return null;
            }
        }

        public String getUserCodeLov() {
            synchronized (ModelFacade.this) {
                if (currentDeposit == null || currentDeposit.userCodeData == null) {
                    return null;
                }
                return currentDeposit.userCodeData.description;
            }
        }

        public String getCurrency() {
            synchronized (ModelFacade.this) {
                if (currentDeposit != null) {
                    return currentDeposit.currencyData.textId;
                }
                return null;
            }
        }

        public List<Bill> getBillData() {
            synchronized (ModelFacade.this) {
                if (currentDeposit != null) {
                    return Bill.getCurrentCounters(currentDeposit.currency);
                }
                return null;
            }
        }

        public String getStatus() {
            if (currentStep == CurrentStep.BILL_DEPOSIT || currentStep == CurrentStep.COUNT) {
                return manager.getStatus().name();
            } else {
                return currentStep.name();
            }
        }
    }

    synchronized public BillDepositStartData getStartBillDepositData() {
        CurrentStep s = getCurrentStep();
        if (s != CurrentStep.BILL_DEPOSIT && s != CurrentStep.BILL_DEPOSIT_FINISH
                && s != CurrentStep.COUNT) {
            error(String.format("getStartBillDepositData Invalid step %s", currentStep.name()));
            return null;
        }
        return new BillDepositStartData();
    }

    synchronized public Deposit getDeposit() {
        return currentDeposit;
    }

    // TODO: Manage the db error here.
    private void saveDeposit() {
        LgBatch batch = new LgBatch();
        for (Bill bill : Bill.getCurrentCounters(currentDeposit.currency)) {
            Logger.debug(" -> quantity %d", bill.quantity);
            LgBillType bt = LgBillType.findById(bill.billTypeId);
            LgBill b = new LgBill(batch, bill.quantity, bt, currentDeposit);
            //batch.bills.add(b);
        }
        batch.save();
    }

/////////////////////////////////////////////////////////////////////////////////////////
    static public enum Operations {

        IDLE,
        CASH_DEPOSIT,
        ENVELOP_DEPOSIT,
        CASH_COUNT,
        CASH_SPLIT,;
    }

    public static class CashDeposit {

        public CashDeposit(LgUser user) {
            screen = Screens.REFERENCE_INPUT;
            deposit_saved = false;
        }

        static public enum Screens {

            REFERENCE_INPUT,
            CASH_DEPOSIT_COUNT,
            CASH_DEPOSIT_CANCEL,
            CASH_DEPOSIT_ACCEPT,;
        }
        public Deposit deposit;
        public Boolean deposit_saved;
        private Screens screen;

        public Boolean readyFor(Screens targetScreen) {
            if (screen == targetScreen) {
                return true;
            }
            Manager.ControllerApi manager = CounterFactory.getGloryManager();
            Manager.Status status = manager.getStatus();

            switch (targetScreen) {
                case REFERENCE_INPUT:
                    return (status == Manager.Status.IDLE) && (!deposit_saved);
                case CASH_DEPOSIT_COUNT:
                    if (!deposit.validateReferenceAndCurrency()) {
                        return false;
                    }
                    if (status != Manager.Status.IDLE) {
                        Logger.error("machine is not idle! %s", manager.getStatus());
                        return false;
                    }
                    return true;
                case CASH_DEPOSIT_CANCEL:
                    return screen == Screens.CASH_DEPOSIT_COUNT;
                case CASH_DEPOSIT_ACCEPT:
                    return (screen == Screens.CASH_DEPOSIT_COUNT)
                            && (status == Manager.Status.READY_TO_STORE);
                default:
                    break;
            };
            return false;
        }

        public Boolean switchTo(Screens targetScreen) {
            assert readyFor(targetScreen);
            Logger.info("in switch to: %s", targetScreen);

            if (screen == targetScreen) {
                return true;
            }

            //we don't want to repeat readyFor() checks here..
            if (!readyFor(targetScreen)) {
                return false;
            }

            Manager.ControllerApi manager = CounterFactory.getGloryManager();

            switch (targetScreen) {
                case REFERENCE_INPUT:
                    return true;
                case CASH_DEPOSIT_COUNT:
                    /*
                     * if (!manager.count(null, deposit.currency)) { return
                     * false; }
                     */
                    break;
                case CASH_DEPOSIT_CANCEL:
                    Logger.info("switching to: %s", targetScreen);
                    //manager.cancelDeposit();
                    // on callback:
                    manager.reset();
                    ModelFacade.get().resetOperation();
                    break;
                case CASH_DEPOSIT_ACCEPT:
                    Logger.info("switching to: %s", targetScreen);
                    Integer what = 42;
                    List<Bill> billData = Bill.getCurrentCounters(
                            deposit.currency);
                    if (!manager.storeDeposit(what)) {
                        // if can't send command now we go back.
                        return false;
                    }
                    // XXX on callback:
                    saveDeposit(billData);
                    closeDeposit();
                    ModelFacade.get().resetOperation();
                default:
                    return false;
            }
            screen = targetScreen;
            return true;
        }

        private void saveDeposit(List<Bill> billData) {
            if (!deposit_saved) {
                deposit.save();
                deposit_saved = true;
            }
            LgBatch batch = new LgBatch();
            for (Bill bill : billData) {
                Logger.debug(" -> quantity %d", bill.quantity);
                LgBillType bt = LgBillType.findById(bill.billTypeId);
                LgBill b = new LgBill(batch, bill.quantity, bt, deposit);
                //batch.bills.add(b);
            }
            batch.save();
        }

        private void closeDeposit() {
            deposit.finishDate = new Date();
            deposit.save();
        }
    }
    //private 
    private ModelFacade.Operations operation = Operations.IDLE;
    private ModelFacade.CashDeposit cashDeposit = null;

    public Operations currentOperation() {
        return operation;
    }

    private Boolean setOperation(Operations newOperation) {
        if (newOperation != Operations.IDLE) {
            if (currentOperation() != Operations.IDLE) {
                return false;
            }
            operation = newOperation;
            return true;
        } else {
            //newOperation is IDLE so..
            operation = Operations.IDLE;
            return true;
        }
    }

    public void resetOperation() {
        assert currentOperation() != Operations.IDLE;
        switch (currentOperation()) {
            case IDLE:
                break;
            case CASH_DEPOSIT:
                cashDeposit = null;
                break;
        }
        setOperation(Operations.IDLE);
    }

    // CashDeposit
    public Boolean CreateCashDeposit() {
        Manager.Status status = manager.getStatus();
        while (status != Manager.Status.IDLE) {
            Logger.error("machine status is not idle! it's: %s", status);
            if (status != Manager.Status.READY_TO_STORE) {
                Logger.error(" -> performing reset!");
                manager.reset();
            }
            return false;
        }

        LgUser user;
        try {
            user = Secure.getCurrentUser();
        } catch (Throwable ex) {
            //
            return false;
        }

        Boolean r = setOperation(Operations.CASH_DEPOSIT);
        if (r) {
            Logger.info("cash deposit created!!!");
            cashDeposit = new CashDeposit(user);
        }
        return r;
    }

    public CashDeposit getCashDeposit() {
        Logger.info("returning cash deposit! ==null? %b", cashDeposit == null);
        return cashDeposit;
    }
}
