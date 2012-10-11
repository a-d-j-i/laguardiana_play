package models;

import java.util.Date;
import javax.persistence.Entity;
import models.db.LgBag;
import models.db.LgBatch;
import models.db.LgBill;
import models.db.LgDeposit;
import models.db.LgEnvelope;
import models.db.LgLov;
import models.db.LgUser;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.db.jpa.JPABase;

@Entity
public class Deposit extends LgDeposit {

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

    public Deposit(LgUser user, String userCode, DepositUserCodeReference userCodeData) {
        this.bag = LgBag.getCurrentBag();
        this.user = user;
        this.userCode = userCode;

        if (userCodeData != null) {
            this.userCodeLov = userCodeData.numericId;
        }
        this.creationDate = new Date();
    }

    @Override
    public <T extends JPABase> T save() {
        boolean mustSave = (!JPABase.em().contains(this));
        T ret = super.save();
        if (mustSave) {
            Event event = new Event(this, Event.Type.DEPOSIT_CHANGE, String.format("Deposit changed by userId : %d", user.userId));
            event.save();
        }
        return ret;
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
                + " TOTAL : " + getTotal(depositId) + " FINISH DATE : " + finishDate;
    }

    public Long getTotal() {
        return Deposit.getTotal(depositId);
    }

    public static Long getTotal(Integer id) {
        Long r = Deposit.find("select sum(b.quantity * bt.denomination) "
                + " from Deposit d, LgBill b, LgBillType bt "
                + " where b.deposit = d and b.billType = bt"
                + " and d.depositId = ?", id).first();
        if (r == null) {
            return new Long(0);
        }
        return r;
    }
}
