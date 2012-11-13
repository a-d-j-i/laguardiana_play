package models;

import controllers.Secure;
import javax.persistence.Entity;
import models.actions.UserAction;
import models.db.LgBag;
import models.db.LgEvent;
import models.db.LgUser;
import play.Logger;

@Entity
public class InvalidBagEvent extends LgEvent {

    public InvalidBagEvent(LgUser user, Integer eventSourceId, String message) {
        super(user, eventSourceId, message);
    }

    public static void save(LgBag bag, String name) {
        Integer bagId = null;
        if (bag != null) {
            bagId = bag.bagId;
        }
        try {
            InvalidBagEvent e = new InvalidBagEvent(Secure.getCurrentUser(), bagId, name);
            e.save();
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
    }
}
