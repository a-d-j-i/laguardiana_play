package models;

import javax.persistence.Entity;
import models.db.LgDeposit;
import models.db.LgEvent;
import models.db.LgUser;
import play.Logger;

@Entity
public class DepositProcessedEvent extends LgEvent {

    public DepositProcessedEvent(LgUser user, Integer eventSourceId, String message) {
        super(user, eventSourceId, message);
    }

    public static DepositProcessedEvent save(LgDeposit deposit, String name) {
        Integer depositId = null;
        User u = null;
        if (deposit != null) {
            depositId = deposit.depositId;
        }
        try {
            DepositProcessedEvent e = new DepositProcessedEvent(u, depositId, name);
            e.save();
            return e;
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
        return null;
    }
}
