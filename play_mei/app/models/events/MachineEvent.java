package models.events;

import javax.persistence.Entity;
import machines.MachineAbstract;
import models.db.LgEvent;
import play.Logger;

@Entity
public class MachineEvent extends LgEvent {

    public MachineEvent(String message) {
        super(null, 0, message);
    }

    public static void save(MachineAbstract m, String format, Object... args) {
        String msg = String.format("MachineEvent : %s -> %s", m.toString(), String.format(format, args));
        Logger.debug(msg);
        try {
            MachineEvent e = new MachineEvent(msg);
            e.save();
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
    }
}
