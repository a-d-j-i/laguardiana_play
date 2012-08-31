/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.manager;

import play.Logger;

import controllers.Secure;
import devices.CounterFactory;
import devices.glory.manager.Manager;
import devices.glory.manager.Manager.Status;
import java.util.Date;
import java.util.List;

import models.Bill;
import models.Deposit;
import models.db.*;

/**
 *
 * @author aweil
 */
public class ModMan {
    static public enum Operations {
        IDLE,
        CASH_DEPOSIT,
        ENVELOP_DEPOSIT,
        CASH_COUNT,
        CASH_SPLIT,;
    }


    public static class CashDeposit {
        public CashDeposit(LgUser user)
        {
            deposit = new Deposit(user);
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
            if (screen==targetScreen)
                return true;
            Manager.ControllerApi manager = CounterFactory.getGloryManager();
            Manager.Status status = manager.getStatus();
                    
            switch(targetScreen) {
                case REFERENCE_INPUT: 
                    return (status==Manager.Status.IDLE) && (!deposit_saved);
                case CASH_DEPOSIT_COUNT:
                    if (!deposit.validateReferenceAndCurrency())
                        return false;
                    if (status!=Manager.Status.IDLE) {
                        Logger.error("machine is not idle! %s",manager.getStatus());
                        return false;
                    }
                    return true;
                case CASH_DEPOSIT_CANCEL:
                    return screen == Screens.CASH_DEPOSIT_COUNT;
                case CASH_DEPOSIT_ACCEPT:
                    return (screen == Screens.CASH_DEPOSIT_COUNT) && 
                                    (status==Manager.Status.READY_TO_STORE);
                default:
                    break;
            };
            return false;
        }
        
        public Boolean switchTo(Screens targetScreen) {
            assert readyFor(targetScreen);
            Logger.info("in switch to: %s", targetScreen);
            
            if (screen==targetScreen)
                return true;
            
            //we don't want to repeat readyFor() checks here..
            if (!readyFor(targetScreen))
                return false;
            
            Manager.ControllerApi manager = CounterFactory.getGloryManager();
            
            switch(targetScreen) {
                case REFERENCE_INPUT:
                    return true;
                case CASH_DEPOSIT_COUNT:
                    if (!manager.count(null, deposit.currency) )
                        return false;
                    break;
                case CASH_DEPOSIT_CANCEL:
                    Logger.info("switching to: %s", targetScreen);
                    manager.cancelDeposit();
                    // on callback:
                    manager.reset();
                    ModMan.get().resetOperation();
                    break;
                case CASH_DEPOSIT_ACCEPT:
                    Logger.info("switching to: %s", targetScreen);
                    Integer what=42;
                    List<Bill> billData = Bill.getCurrentCounters(
                                                            deposit.currency);
                    if (!manager.storeDeposit(what))
                    {
                        // if can't send command now we go back.
                        return false; 
                    }
                    // XXX on callback:
                    saveDeposit(billData);
                    closeDeposit();
                    ModMan.get().resetOperation();
                default:
                    return false;
            }
            screen = targetScreen;
            return true;
        }
        
        private void saveDeposit (List<Bill> billData) {
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
    static private ModMan THIS = null;
    private ModMan.Operations operation = Operations.IDLE;
    private ModMan.CashDeposit cashDeposit = null;
    
    
    static public ModMan get() {
        if (THIS==null){
            THIS = new ModMan();
        }
        return THIS;
    }
    
    public Operations currentOperation() {
        return operation;
    }
    
    private Boolean setOperation(Operations newOperation) {
        if (newOperation!=Operations.IDLE)
        {
            if (currentOperation()!=Operations.IDLE) {
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
        assert currentOperation()!=Operations.IDLE;
        switch(currentOperation()) {
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
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        Manager.Status status = manager.getStatus();
        while (status!=Manager.Status.IDLE) 
        {
            Logger.error("machine status is not idle! it's: %s", status);
            if (status!=Manager.Status.READY_TO_STORE) {
                Logger.error(" -> performing reset!");
                manager.reset();
            }
            return false;
        }
        
        LgUser user ;
        try{
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
        Logger.info("returning cash deposit! ==null? %b", cashDeposit==null);
        return cashDeposit;
    }
}
