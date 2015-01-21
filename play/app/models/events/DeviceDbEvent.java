package models.events;

import devices.device.DeviceAbstract;
import javax.persistence.Entity;
import models.db.LgEvent;
import play.Logger;

@Entity
public class DeviceDbEvent extends LgEvent {

    public DeviceDbEvent(String message) {
        super(null, 0, message);
    }

    public static void save(DeviceAbstract d, String format, Object... args) {
        String msg = String.format("Device : %s -> %s", d.toString(), String.format(format, args));
        Logger.debug(msg);
        try {
            DeviceDbEvent e = new DeviceDbEvent(msg);
            e.save();
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
    }
}
