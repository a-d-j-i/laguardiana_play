package models;

import javax.persistence.Entity;
import models.db.LgEvent;
import models.db.LgUser;
import models.db.LgZ;
import play.Logger;

@Entity
public class ZProcessedEvent extends LgEvent {

    public ZProcessedEvent(LgUser user, Integer eventSourceId, String message) {
        super(user, eventSourceId, message);
    }

    public static ZProcessedEvent save(LgZ z, String name) {
        Integer zId = null;
        User u = null;
        if (z != null) {
            zId = z.zId;
        }
        try {
            ZProcessedEvent e = new ZProcessedEvent(u, zId, name);
            Logger.error("----------------------------- saving event.");
            
            e.save();
            return e;
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
        return null;
    }
}
