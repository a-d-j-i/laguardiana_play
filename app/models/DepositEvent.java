package models;

import javax.persistence.Entity;
import models.db.LgDeposit;
import models.db.LgEvent;
import models.db.LgUser;
import play.Logger;

@Entity
public class DepositEvent extends LgEvent {

    public DepositEvent(LgUser user, Integer eventSourceId, String message) {
        super(user, eventSourceId, message);
    }

    public static DepositEvent save(LgUser u, LgDeposit deposit, String name) {
        Integer depositId = null;
        if (deposit != null) {
            depositId = deposit.depositId;
        }
        try {
            DepositEvent e = new DepositEvent(u, depositId, name);
            e.save();
            return e;
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
        return null;
    }
}
