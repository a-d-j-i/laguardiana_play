package models.events;

import controllers.Secure;
import javax.persistence.Entity;
import models.db.LgBag;
import models.db.LgEvent;
import models.db.LgUser;
import play.Logger;

@Entity
public class BagEvent extends LgEvent {

    public BagEvent(LgUser user, Integer eventSourceId, String message) {
        super(user, eventSourceId, message);
    }

    public static void save(LgBag bag, String name) {
        Integer bagId = null;
        if (bag != null) {
            bagId = bag.bagId;
        }
        try {
            BagEvent e = new BagEvent(Secure.getCurrentUser(), bagId, name);
            e.save();
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
    }
}
