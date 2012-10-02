package models;

import javax.persistence.*;
import models.db.*;

@Entity
public class Event extends LgEvent implements java.io.Serializable {

    public static enum Type {

        GLORY(1),
        ACTION_START_TRY(2),
        ACTION_START(4),
        ACTION_FINISH(5),
        DEPOSIT_CHANGE(6),
        IO_BOARD(7),
        INVALID_BAG(8),
        TIMEOUT(9),;
        private Integer eventTypeLov;

        private Type(Integer eventTypeLov) {
            this.eventTypeLov = eventTypeLov;
        }

        private Integer getEventTypeLov() {
            return eventTypeLov;
        }
    }

    public Event(Deposit deposit, Type type, String message) {
        this.deposit = deposit;
        this.eventTypeLov = type.getEventTypeLov();
        this.message = message;
    }

    public static void save(Integer depositId, Type type, String message) {
        Deposit deposit = null;
        if (depositId != null) {
            deposit = Deposit.findById(depositId);
        }
        Event e = new Event(deposit, type, message);
        e.save();
    }

    @Override
    public String toString() {
        return "LgEvent{" + "eventId=" + eventId + ", deposit=" + deposit + ", eventTypeLov=" + eventTypeLov + ", creationDate=" + creationDate + ", message=" + message + '}';
    }
}
