package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import models.Deposit;
import play.db.jpa.GenericModel;

@Entity
@Table( name = "lg_event", schema = "public")
public class LgEvent extends GenericModel implements java.io.Serializable {

    public static enum Type {

        GLORY(1),
        ACTION_START_TRY(2),
        ACTION_START(4),
        ACTION_FINISH(5);
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
    @Temporal( TemporalType.DATE)
    @Column( name = "creation_date", nullable = false, length = 13)
    public Date creationDate = new Date();
    @Column( name = "message", nullable = false, length = 256)
    public String message;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "event")
    public Set<LgExternalAppLog> externalAppLogs = new HashSet<LgExternalAppLog>(0);

    public static void save(Deposit deposit, Type type, String message) {
        LgEvent e = new LgEvent(deposit, type, message);
        e.save();
    }

    public LgEvent(Deposit deposit, Type type, String message) {
        this.deposit = deposit;
        this.eventTypeLov = type.getEventTypeLov();
        this.message = message;
    }
}
