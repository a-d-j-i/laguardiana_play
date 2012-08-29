package models;

import java.util.Date;
import javax.persistence.Entity;
import models.db.*;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;

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
}
