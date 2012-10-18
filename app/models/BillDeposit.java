package models;

import javax.persistence.Entity;
import models.db.LgDeposit;
import models.db.LgLov;
import models.db.LgUser;
import models.lov.DepositUserCodeReference;

@Entity
public class BillDeposit extends LgDeposit {

    public BillDeposit(LgUser user, String userCode, DepositUserCodeReference userCodeData) {
        super(user, userCode, userCodeData);
    }

    public Long getTotal() {
        Long r = BillDeposit.find("select sum(b.quantity * bt.denomination) "
                + " from BillDeposit d, LgBill b, LgBillType bt "
                + " where b.deposit = d and b.billType = bt"
                + " and d.depositId = ?", depositId).first();
        if (r == null) {
            return new Long(0);
        }
        return r;
    }

    @Override
    public String toString() {
        LgLov uc = this.findUserCodeLov();
        Integer billcount = this.bills.size();
        return "Bill Deposit by: " + user.toString() 
                + " CREATION DATE : " + creationDate
                + " codes:[" + billcount.toString() + ":" + userCode + "/" + uc.toString() + "]"
                + " TOTAL : " + getTotal() + " FINISH DATE : " + finishDate + " in bag: " + bag.toString();
    }
}
