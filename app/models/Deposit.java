package models;

import java.util.Date;
import javax.persistence.Entity;
import models.db.*;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;

@Entity
public class Deposit extends LgDeposit {

    // TODO: Validate the depositId there are allways only one deposit ID
    // unfinished.
    public static Deposit getAndValidateOpenDeposit(String depositId) {
        Deposit d = Deposit.findById(Integer.parseInt(depositId));
        if (d.finishDate != null) {
            return null;
        }
        return d;
    }

    public Deposit() {
    }

    public void addBill(LgBill bill) {
        bill.addToDeposit(this);
        this.bills.add(bill);
    }

    public void addBatch(LgBatch batch) {
        for (LgBill bill : batch.bills) {
            this.addBill(bill);
            bill.save();
        }
    }

    public Deposit(LgUser user, String userCode, LgLov userCodeLov, Currency currency) {
        this.bag = LgBag.GetCurrentBag();
        this.user = user;
        this.userCode = userCode;
        this.userCodeLov = userCodeLov.numericId;
        this.creationDate = new Date();
        this.currency = currency.numericId;
    }

    public Deposit(LgUser user) {
        this.bag = LgBag.GetCurrentBag();
        this.user = user;
        this.creationDate = new Date();
    }

    public LgLov findUserCodeLov() {
        return DepositUserCodeReference.findByNumericId(userCodeLov);
    }

    @Override
    public String toString() {
        LgLov uc = this.findUserCodeLov();
        Integer billcount = this.bills.size();
        return "Deposit by: " + user.toString() + " in: " + bag.toString()
                + " codes:[" + billcount.toString() + ":" + userCode + "/" + uc.toString() + "]";
    }

    public Boolean validateReferenceAndCurrency() {
        if ((userCode == null) || (userCodeLov == null) || (currency == null)) {
            Logger.error("usercode null? %b", userCode == null);
            Logger.error("usercodelov null? %b", userCodeLov == null);
            Logger.error("currency null? %b", currency == null);
            return false;
        }
        // empty 
        if ((userCode.isEmpty())) // || (userCodeLov.isEmpty()))
        {
            return false;
        }
        Currency c = controllers.Application.validateCurrency(currency);
        if (c == null) {
            return false;
        }
        return true;
    }

    public static Currency validateCurrency(Integer currency) {
        // Validate Currency.
        if (currency == null) {
            try {
                String dc = LgSystemProperty.getProperty("bill_deposit.default_currency");
                currency = Integer.parseInt(dc);
            } catch (NumberFormatException e) {
                currency = 1; // Fixed.
            }
        }
        Currency c = Currency.findByNumericId(currency);
        if (c == null) {
            Logger.error("validateCurrency: invalid currency %d", currency);
            return null;
        }
        return c;
    }
}
