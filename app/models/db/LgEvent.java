package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import models.actions.UserAction;
import play.Logger;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_event", schema = "public")
public class LgEvent extends GenericModel implements java.io.Serializable {

    public static enum Type {

        GLORY(1),
        ACTION_START_TRY(2),
        ACTION_START(4),
        ACTION_FINISH(5),
        DEPOSIT_CHANGE(6),
        IO_BOARD(7),
        INVALID_BAG(8),
        TIMEOUT(9),
        DEPOSIT_EXPORT(10),;
        private Integer eventTypeLov;

        private Type(Integer eventTypeLov) {
            this.eventTypeLov = eventTypeLov;
        }

        private Integer getEventTypeLov() {
            return eventTypeLov;
        }
    }
    @Id
    @Column( name = "event_id", unique = true, nullable = false)
    @GeneratedValue
    public Integer eventId;
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn( name = "deposit_id")
    public LgDeposit deposit;
    @Column( name = "event_type_lov", nullable = true)
    public Integer eventTypeLov;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "creation_date", nullable = false, length = 13)
    public Date creationDate = new Date();
    @Column( name = "message", nullable = true, length = 256)
    public String message;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "event")
    public Set<LgExternalAppLog> externalAppLogs = new HashSet<LgExternalAppLog>(0);

    public LgEvent(Type type, String message) {
        this.eventTypeLov = type.getEventTypeLov();
        this.message = message;
    }

    public static void save(UserAction userAction, Type type, String message) {
        LgDeposit deposit = null;
        if (userAction != null && userAction.getDepositId() != null) {
            deposit = LgDeposit.findById(userAction.getDepositId());
        }
        try {
            LgEvent e = new LgEvent(type, message);
            e.setDeposit(deposit);
            e.save();
        } catch (Exception ex) {
            Logger.error("Error saving event %s", ex);
        }
    }

    public void setDeposit(LgDeposit d) {
        if (d != null) {
            d.events.add(this);
            this.deposit = d;
        }
//        is nulable!!!
//        else {
//            Logger.error("Event deposit null");
//        }
    }

    @Override
    public String toString() {
        return "LgEvent{" + "eventId=" + eventId + ", deposit=" + deposit + ", eventTypeLov=" + eventTypeLov + ", creationDate=" + creationDate + ", message=" + message + '}';
    }
}
