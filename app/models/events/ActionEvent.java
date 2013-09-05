package models.events;

import javax.persistence.Entity;
import models.actions.UserAction;
import models.db.LgEvent;
import models.db.LgUser;
import play.Logger;

@Entity
public class ActionEvent extends LgEvent {

    public ActionEvent(LgUser user, Integer eventSourceId, String message) {
        super(user, eventSourceId, message);
    }

    public static void save(UserAction userAction, String msg, String controller) {
        Integer depositId = null;
        LgUser u = null;
        if (userAction != null) {
            depositId = userAction.getDepositId();
            u = userAction.getCurrentUser();
        }
        try {
            ActionEvent e = new ActionEvent(u, depositId, msg + controller);
            e.save();
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
    }
}
