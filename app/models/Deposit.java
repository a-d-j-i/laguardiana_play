package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.PostPersist;
import models.db.*;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.db.jpa.JPABase;

@Entity
public class Deposit extends LgDeposit {

    transient public final DepositUserCodeReference userCodeData;
    transient public final Currency currency;

    // TODO: Validate the depositId there are allways only one deposit ID
    // unfinished.
//    public static Deposit getAndValidateOpenDeposit(String depositId) {
//        Deposit d = Deposit.findById(Integer.parseInt(depositId));
//        if (d.finishDate != null) {
//            return null;
//        }
//        return d;
//    }
    public void addBatch(LgBatch batch) {
        for (LgBill bill : batch.bills) {
            bills.add(bill);
            bill.deposit = this;
        }
    }

    public void addEnvelope(LgEnvelope envelope) {
        envelopes.add(envelope);
        envelope.deposit = this;
    }

    public Deposit(LgUser user, String userCode, DepositUserCodeReference userCodeData, Currency currency) {
        this.bag = LgBag.getCurrentBag();
        this.user = user;
        this.userCode = userCode;

        this.userCodeData = userCodeData;
        if (userCodeData != null) {
            this.userCodeLov = userCodeData.numericId;
        }
        this.creationDate = new Date();
        this.currency = currency;
    }

    @Override
    public <T extends JPABase> T save() {
        if (!JPABase.em().contains(this)) {
            LgEvent event = new LgEvent(this, LgEvent.Type.DEPOSIT_CHANGE, String.format("Deposit changed by userId : %d", user.userId));
        }
        return super.save();
    }

    public LgLov findUserCodeLov() {
        return DepositUserCodeReference.findByNumericId(userCodeLov);
    }

    @Override
    public String toString() {
        LgLov uc = this.findUserCodeLov();
        Integer billcount = this.bills.size();
        return "Deposit by: " + user.toString() + " in: " + bag.toString()
                + " codes:[" + billcount.toString() + ":" + userCode + "/" + uc.toString() + "]"
                + " TOTAL : " + getTotal() + " FINISH DATE : " + finishDate;
    }

    public Long getTotal() {
        Long r = Deposit.find("select sum(b.quantity * bt.denomination) "
                + " from Deposit d, LgBill b, LgBillType bt "
                + " where b.deposit = d and b.billType = bt"
                + " and d.depositId = ?", depositId).first();
        if (r == null) {
            return new Long(0);
        }
        return r;
    }
}
