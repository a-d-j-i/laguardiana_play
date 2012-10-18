package models;

import javax.persistence.Entity;
import models.db.LgDeposit;
import models.db.LgLov;
import models.db.LgUser;
import models.lov.DepositUserCodeReference;

@Entity
public class EnvelopeDeposit extends LgDeposit {

    public EnvelopeDeposit(LgUser user, String userCode, DepositUserCodeReference userCodeData) {
        super(user, userCode, userCodeData);
    }

    @Override
    public String toString() {
        LgLov uc = this.findUserCodeLov();
        Integer billcount = this.bills.size();
        return "Envelope Deposit by: " + user.toString()
                + " CREATION DATE : " + creationDate
                + " codes:[" + billcount.toString() + ":" + userCode + "/" + uc.toString() + "]"
                + " FINISH DATE : " + finishDate 
                + " envelopes : " + envelopes  + " in bag: " + bag.toString();
    }
}
