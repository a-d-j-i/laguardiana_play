package models.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import play.db.jpa.GenericModel;

@Entity
@Table(name = "lg_event", schema = "public")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type", length = 32)
abstract public class LgEvent extends GenericModel implements java.io.Serializable {

    /*
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
     }*/
    @Id
    @Column( name = "event_id", unique = true, nullable = false)
    @GeneratedValue
    public Integer eventId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    public LgUser user;
    @Column(name = "event_source_id", nullable = true)
    public Integer eventSourceId;
    @Temporal( TemporalType.TIMESTAMP)
    @Column( name = "creation_date", nullable = false, length = 13)
    public Date creationDate = new Date();
    @Column( name = "message", nullable = true, length = 256)
    public String message;
    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "event")
    public Set<LgExternalAppLog> externalAppLogs = new HashSet<LgExternalAppLog>(0);

    public LgEvent(LgUser user, Integer eventSourceId, String message) {
        this.user = user;
        this.message = message;
        this.eventSourceId = eventSourceId;
    }
}
