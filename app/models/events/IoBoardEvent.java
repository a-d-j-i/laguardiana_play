package models.events;

import javax.persistence.Entity;
import models.User;
import models.actions.UserAction;
import models.db.LgEvent;
import models.db.LgUser;
import play.Logger;

@Entity
public class IoBoardEvent extends LgEvent {

    public IoBoardEvent(LgUser user, Integer eventSourceId, String message) {
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
            IoBoardEvent e = new IoBoardEvent(u, depositId, name);
            e.save();
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
    }
}
