package models;

import javax.persistence.Entity;
import models.actions.UserAction;
import models.db.LgEvent;
import models.db.LgUser;
import play.Logger;

@Entity
public class TimeoutEvent extends LgEvent {

    public TimeoutEvent(LgUser user, Integer eventSourceId, String message) {
        super(user, eventSourceId, message);
    }

    public static void save(UserAction userAction, String name) {
        Integer depositId = null;
        User u = null;
        if (userAction != null) {
            depositId = userAction.getDepositId();
            u = userAction.getCurrentUser();
        }
        try {
            TimeoutEvent e = new TimeoutEvent(u, depositId, name);
            e.save();
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
    }
}
