package models.events;

import devices.printer.Printer;
import javax.persistence.Entity;
import models.db.LgEvent;
import play.Logger;

@Entity
public class PrinterEvent extends LgEvent {

    public PrinterEvent(String message) {
        super(null, 0, message);
    }

    public static void save(Printer m, String format, Object... args) {
        String msg = String.format("PrinterEvent : %s -> %s", m.toString(), String.format(format, args));
        Logger.debug(msg);
        try {
            PrinterEvent e = new PrinterEvent(msg);
            e.save();
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
    }
}
