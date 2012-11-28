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
}
