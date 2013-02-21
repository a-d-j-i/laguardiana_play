package models.events;

import javax.persistence.Entity;
import models.User;
import models.db.LgBag;
import models.db.LgEvent;
import models.db.LgUser;
import play.Logger;

@Entity
public class BagProcessedEvent extends LgEvent {

    public BagProcessedEvent(LgUser user, Integer eventSourceId, String message) {
        super(user, eventSourceId, message);
    }

    public static BagProcessedEvent save(LgBag bag, String name) {
        Integer bagId = null;
        User u = null;
        if (bag != null) {
            bagId = bag.bagId;
        }
        try {
            BagProcessedEvent e = new BagProcessedEvent(u, bagId, name);
            e.save();
            return e;
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
        return null;
    }
}
