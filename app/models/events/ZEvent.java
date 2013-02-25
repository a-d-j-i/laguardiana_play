package models.events;

import controllers.Secure;
import javax.persistence.Entity;
import models.db.LgEvent;
import models.db.LgUser;
import models.db.LgZ;
import play.Logger;

@Entity
public class ZEvent extends LgEvent {

    public ZEvent(LgUser user, Integer eventSourceId, String message) {
        super(user, eventSourceId, message);
    }

    public static void save(LgZ z, String name) {
        Integer zId = null;
        if (z != null) {
            zId = z.zId;
        }
        try {
            ZEvent e = new ZEvent(Secure.getCurrentUser(), zId, name);
            e.save();
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
    }
}
